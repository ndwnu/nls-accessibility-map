package nu.ndw.nls.accessibilitymap.accessibility.service.debug;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportConditions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.service.debug.RestrictionProperties.RestrictionPropertiesBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.service.debug.configuration.DebugConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.geojson.geometry.mappers.JtsLineStringJsonMapper;
import nu.ndw.nls.geojson.geometry.mappers.JtsPointJsonMapper;
import nu.ndw.nls.geojson.geometry.mappers.JtsPolygonJsonMapper;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.Feature;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.FeatureCollection;
import org.apache.commons.io.FileUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccessibilityDebugger {

    private static final int METERS_PER_DEGREE = 111_320;

    private static final int CIRCLE_RESOLUTION = 64;

    private final DebugConfiguration debugConfiguration;

    private final JtsPointJsonMapper jtsPointJsonMapper;

    private final JtsLineStringJsonMapper jtsLineStringJsonMapper;

    private final JtsPolygonJsonMapper jtsPolygonJsonMapper;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    public void writeDebug(Accessibility accessibility) {

        if (debugConfiguration.isDisabled()) {
            return;
        }

        writeDebug("accessibility.roadSectionsWithoutRestrictions", accessibility.accessibleRoadsSectionsWithoutAppliedRestrictions());
        writeDebug("accessibility.roadSectionsWithRestrictions", accessibility.accessibleRoadSectionsWithAppliedRestrictions());
        writeDebug("accessibility.unroutableRoadSections", accessibility.unroutableRoadSections());
        writeDebug("accessibility.combinedAccessibility", accessibility.combinedAccessibility());
    }

    public void writeDebug(String name, Collection<RoadSection> roadSections) {

        if (debugConfiguration.isDisabled()) {
            return;
        }

        AtomicLong idSupplier = new AtomicLong(1);
        FeatureCollection featureCollection = FeatureCollection.builder()
                .features(roadSections.stream()
                        .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                        .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                        .map(directionalSegment -> Feature.builder()
                                .id(idSupplier.getAndIncrement())
                                .geometry(jtsLineStringJsonMapper.map(directionalSegment.getLineString()))
                                .properties(RoadSectionSegmentProperties.builder()
                                        .roadSectionId(directionalSegment.getRoadSectionFragment().getRoadSection().getId())
                                        .roadSectionFragmentId(directionalSegment.getRoadSectionFragment().getId())
                                        .edge(directionalSegment.getRoadSectionFragment().getId())
                                        .segmentId(directionalSegment.getId())
                                        .edgeKey(directionalSegment.getId())
                                        .direction(directionalSegment.getDirection())
                                        .startFraction(directionalSegment.getStartFraction())
                                        .endFraction(directionalSegment.getEndFraction())
                                        .accessible(directionalSegment.isAccessible())
                                        .build())
                                .build())
                        .toList())
                .build();

        writeGeoJson(name, featureCollection);
    }

    public void writeDebug(Restrictions restrictions) {

        if (debugConfiguration.isDisabled()) {
            return;
        }

        AtomicLong idSupplier = new AtomicLong(1);
        FeatureCollection featureCollection = FeatureCollection.builder()
                .features(restrictions.stream()
                        .map(restriction -> {
                            RestrictionPropertiesBuilder restrictionPropertiesBuilder = RestrictionProperties.builder()
                                    .type(restriction.getClass().getSimpleName())
                                    .roadSectionId(restriction.roadSectionId())
                                    .direction(restriction.direction())
                                    .fraction(restriction.fraction());

                            if (restriction instanceof TrafficSign trafficSignRestriction) {

                                ConditionsProperties restrictionConditions;
                                List<ConditionsProperties> exemptionsConditions;
                                if (trafficSignRestriction.transportRestrictions() != null) {
                                    restrictionConditions = mapConditionProperties(trafficSignRestriction.transportRestrictions()
                                            .restrictions());
                                    exemptionsConditions = trafficSignRestriction.transportRestrictions().exemptions()
                                            .stream()
                                            .map(this::mapConditionProperties)
                                            .toList();
                                } else {
                                    restrictionConditions = null;
                                    exemptionsConditions = null;
                                }

                                restrictionPropertiesBuilder
                                        .trafficSignId(trafficSignRestriction.id())
                                        .trafficSignExternalId(trafficSignRestriction.externalId())
                                        .trafficSignType(trafficSignRestriction.trafficSignType())
                                        .restrictions(restrictionConditions)
                                        .exemptions(exemptionsConditions);
                            }

                            return Feature.builder()
                                    .id(idSupplier.getAndIncrement())
                                    .geometry(jtsPointJsonMapper.map(geometryFactory.createPoint(new Coordinate(
                                            restriction.networkSnappedLongitude(),
                                            restriction.networkSnappedLatitude()))))
                                    .properties(restrictionPropertiesBuilder.build())
                                    .build();
                        })
                        .toList())
                .build();

        writeGeoJson("activeRestriction", featureCollection);
    }

    private ConditionsProperties mapConditionProperties(TransportConditions transportConditions) {
        return ConditionsProperties.builder()
                .transportTypes(transportConditions.transportTypes())
                .categories(transportConditions.categories())
                .timeValidity(transportConditions.timeValidity())
                .emissionClass(transportConditions.emissionClass())
                .fuelType(transportConditions.fuelType())
                .vehicleLengthInCm(getMaximumValue(transportConditions.vehicleLengthInCm()))
                .vehicleHeightInCm(getMaximumValue(transportConditions.vehicleHeightInCm()))
                .vehicleWidthInCm(getMaximumValue(transportConditions.vehicleWidthInCm()))
                .vehicleWeightInKg(getMaximumValue(transportConditions.vehicleWeightInKg()))
                .vehicleAxleLoadInKg(getMaximumValue(transportConditions.vehicleAxleLoadInKg()))
                .build();
    }

    private Double getMaximumValue(Maximum maximum) {
        if (maximum == null) {
            return null;
        }
        return maximum.value();
    }

    public void writeDebug(AccessibilityRequest accessibilityRequest) {
        if (debugConfiguration.isDisabled()) {
            return;
        }

        AtomicLong idSupplier = new AtomicLong(1);
        FeatureCollection featureCollection = FeatureCollection.builder()
                .features(Stream.of(
                                buildArea(idSupplier, "requestArea", accessibilityRequest.requestArea()),
                                buildArea(idSupplier, "searchArea", accessibilityRequest.searchArea()),
                                buildPoint(
                                        idSupplier,
                                        "from",
                                        accessibilityRequest.startLocationLatitude(),
                                        accessibilityRequest.startLocationLongitude()),
                                buildPoint(
                                        idSupplier,
                                        "destination",
                                        accessibilityRequest.endLocationLatitude(),
                                        accessibilityRequest.endLocationLongitude()),
                                buildCircle(
                                        idSupplier,
                                        "searchRadius",
                                        accessibilityRequest.startLocationLatitude(),
                                        accessibilityRequest.startLocationLongitude(),
                                        accessibilityRequest.maxSearchDistanceInMeters())
                        )
                        .flatMap(Optional::stream)
                        .toList())
                .build();

        writeGeoJson("accessibilityRequest", featureCollection);
    }

    public void writeDebug(AccessibilityNetwork accessibilityNetwork) {
        if (debugConfiguration.isDisabled()) {
            return;
        }

        Snap destination = accessibilityNetwork.getDestination();
        AtomicLong idSupplier = new AtomicLong(1);
        FeatureCollection featureCollection = FeatureCollection.builder()
                .features(Stream.of(
                                buildPoint(
                                        idSupplier,
                                        "from",
                                        accessibilityNetwork.getFrom().getSnappedPoint().getLat(),
                                        accessibilityNetwork.getFrom().getSnappedPoint().getLon()),
                                buildPoint(
                                        idSupplier,
                                        "destination",
                                        Objects.nonNull(destination) ? destination.getSnappedPoint().getLat() : null,
                                        Objects.nonNull(destination) ? destination.getSnappedPoint().getLon() : null)
                        )
                        .flatMap(Optional::stream)
                        .toList())
                .build();

        writeGeoJson("accessibilityNetwork", featureCollection);
    }

    public void writeDebug(List<DirectionalSegment> path) {
        if (debugConfiguration.isDisabled()) {
            return;
        }

        AtomicLong idSupplier = new AtomicLong(1);
        FeatureCollection featureCollection = FeatureCollection.builder()
                .features(path.stream()
                        .map(directionalSegment -> Feature.builder()
                                .id(idSupplier.getAndIncrement())
                                .geometry(jtsLineStringJsonMapper.map(directionalSegment.getLineString()))
                                .properties(RoadSectionSegmentProperties.builder()
                                        .roadSectionId(directionalSegment.getRoadSectionFragment().getRoadSection().getId())
                                        .roadSectionFragmentId(directionalSegment.getRoadSectionFragment().getId())
                                        .edge(directionalSegment.getRoadSectionFragment().getId())
                                        .segmentId(directionalSegment.getId())
                                        .edgeKey(directionalSegment.getId())
                                        .direction(directionalSegment.getDirection())
                                        .startFraction(directionalSegment.getStartFraction())
                                        .endFraction(directionalSegment.getEndFraction())
                                        .accessible(directionalSegment.isAccessible())
                                        .build())
                                .build())
                        .toList())
                .build();

        writeGeoJson("destinationPaths", featureCollection);
    }

    public void writeDebug(QueryGraph queryGraph) {
        if (debugConfiguration.isDisabled()) {
            return;
        }

        writeGraphHopperNodes(queryGraph);
        writeGraphHopperEdges(queryGraph);
    }

    private void writeGraphHopperNodes(QueryGraph queryGraph) {
        AtomicLong idSupplier = new AtomicLong(1);
        ArrayList<Feature> nodes = new ArrayList<>();

        NodeAccess nodeAccess = queryGraph.getNodeAccess();
        for (int nodeId = 0; nodeId < queryGraph.getNodes(); nodeId++) {
            double latitude = nodeAccess.getLat(nodeId);
            double longitude = nodeAccess.getLon(nodeId);

            nodes.add(Feature.builder()
                    .id(idSupplier.getAndIncrement())
                    .geometry(jtsPointJsonMapper.map(geometryFactory.createPoint(new Coordinate(longitude, latitude))))
                    .properties(GraphHopperNodeProperties.builder()
                            .id(nodeId)
                            .build())
                    .build());
        }
        FeatureCollection featureCollection = FeatureCollection.builder()
                .features(nodes)
                .build();
        writeGeoJson("graphHopper.nodes", featureCollection);
    }

    private void writeGraphHopperEdges(QueryGraph queryGraph) {
        AtomicLong idSupplier = new AtomicLong(1);
        ArrayList<Feature> edgeFeatures = new ArrayList<>();

        EdgeExplorer explorer = queryGraph.createEdgeExplorer();

        for (int node = 0; node < queryGraph.getNodes(); node++) {
            EdgeIterator edgeIterator = explorer.setBaseNode(node);

            while (edgeIterator.next()) {
                int toNode = edgeIterator.getAdjNode();
                EdgeIteratorState currentEdge = queryGraph.getEdgeIteratorState(
                        edgeIterator.getEdge(),
                        toNode);
                double distance = edgeIterator.getDistance();

                PointList geometry = edgeIterator.fetchWayGeometry(FetchMode.ALL);
                edgeFeatures.add(Feature.builder()
                        .id(idSupplier.getAndIncrement())
                        .geometry(jtsLineStringJsonMapper.map(geometry.toLineString(false)))
                        .properties(GraphHopperEdgeProperties.builder()
                                .edge(currentEdge.getEdge())
                                .edgeKey(currentEdge.getEdgeKey())
                                .fromNode(node)
                                .toNode(toNode)
                                .distance(distance)
                                .build())
                        .build());
            }
        }
        FeatureCollection featureCollection = FeatureCollection.builder()
                .features(edgeFeatures)
                .build();
        writeGeoJson("graphHopper.edges", featureCollection);
    }

    private Optional<Feature> buildPoint(AtomicLong idSupplier, String name, Double latitude, Double longitude) {
        if (Objects.isNull(latitude) || Objects.isNull(longitude)) {
            return Optional.empty();
        }
        return Optional.of(Feature.builder()
                .id(idSupplier.getAndIncrement())
                .geometry(jtsPointJsonMapper.map(geometryFactory.createPoint(new Coordinate(longitude, latitude))))
                .properties(NameProperties.builder()
                        .name(name)
                        .build())
                .build());
    }

    private Optional<Feature> buildCircle(AtomicLong idSupplier, String name, Double latitude, Double longitude, double radius) {
        if (Objects.isNull(latitude) || Objects.isNull(longitude)) {
            return Optional.empty();
        }

        Coordinate[] circleCoordinates = createCircleCoordinates(longitude, latitude, radius);

        return Optional.of(Feature.builder()
                .id(idSupplier.getAndIncrement())
                .geometry(jtsPolygonJsonMapper.map(geometryFactory.createPolygon(circleCoordinates)))
                .properties(NameProperties.builder()
                        .name(name)
                        .build())
                .build());
    }

    @SuppressWarnings("java:S109")
    private Coordinate[] createCircleCoordinates(double centerLongitude, double centerLatitude, double radiusInMeters) {

        Coordinate[] coordinates = new Coordinate[CIRCLE_RESOLUTION + 1];

        // Approximate conversion: 1 degree latitude ≈ 111,320 meters
        // 1 degree longitude ≈ 111,320 * cos(latitude) meters
        double latRadiusInDegrees = radiusInMeters / METERS_PER_DEGREE;
        double lonRadiusInDegrees = radiusInMeters / (METERS_PER_DEGREE * Math.cos(Math.toRadians(centerLatitude)));

        for (int i = 0; i < CIRCLE_RESOLUTION; i++) {
            double angle = 2 * Math.PI * i / CIRCLE_RESOLUTION;
            double lon = centerLongitude + lonRadiusInDegrees * Math.cos(angle);
            double lat = centerLatitude + latRadiusInDegrees * Math.sin(angle);
            coordinates[i] = new Coordinate(lon, lat);
        }

        // Close the circle by repeating the first coordinate
        coordinates[CIRCLE_RESOLUTION] = coordinates[0];

        return coordinates;
    }

    private Optional<Feature> buildArea(AtomicLong idSupplier, String name, BBox area) {
        return Optional.of(Feature.builder()
                .id(idSupplier.getAndIncrement())
                .geometry(jtsPolygonJsonMapper.map(geometryFactory.createPolygon(new Coordinate[]{
                        new Coordinate(area.minLon, area.minLat),
                        new Coordinate(area.maxLon, area.minLat),
                        new Coordinate(area.maxLon, area.maxLat),
                        new Coordinate(area.minLon, area.maxLat),
                        new Coordinate(area.minLon, area.minLat),
                })))
                .properties(NameProperties.builder()
                        .name(name)
                        .build())
                .build());
    }

    private void writeGeoJson(String name, FeatureCollection featureCollection) {
        try {
            JsonMapper mapper = JsonMapper.builder().build();

            debugConfiguration.getDebugFolder().toFile().mkdirs();
            FileUtils.writeStringToFile(
                    debugConfiguration.getDebugFolder().resolve(name + ".geojson").toFile(),
                    mapper.writeValueAsString(featureCollection),
                    StandardCharsets.UTF_8.toString());
        } catch (JacksonException exception) {
            log.error("Failed to write accessibility request data to file.", exception);
        } catch (IOException exception) {
            log.error("Failed to write file.", exception);
        }
    }
}
