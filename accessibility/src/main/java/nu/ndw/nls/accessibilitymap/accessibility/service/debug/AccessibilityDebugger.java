package nu.ndw.nls.accessibilitymap.accessibility.service.debug;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.shapes.BBox;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
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
                                restrictionPropertiesBuilder
                                        .trafficSignId(trafficSignRestriction.id())
                                        .trafficSignExternalId(trafficSignRestriction.externalId())
                                        .trafficSignType(trafficSignRestriction.trafficSignType())
                                        .trafficSignBlackCode(trafficSignRestriction.blackCode());
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
                        .filter(Optional::isPresent)
                        .map(Optional::get)
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
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList())
                .build();

        writeGeoJson("accessibilityNetwork", featureCollection);
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
            ObjectMapper mapper = JsonMapper.builder().build();

            debugConfiguration.getDebugFolder().toFile().mkdirs();
            FileUtils.writeStringToFile(
                    debugConfiguration.getDebugFolder().resolve(name + ".geojson").toFile(),
                    mapper.writeValueAsString(featureCollection),
                    StandardCharsets.UTF_8.toString());
        } catch (JsonProcessingException exception) {
            log.error("Failed to write accessibility request data to file.", exception);
        } catch (IOException exception) {
            log.error("Failed to write file.", exception);
        }
    }
}
