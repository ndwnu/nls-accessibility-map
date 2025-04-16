package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.writers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.utils.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.LineStringGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.PointGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.PolygonGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.PolygonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.RoadSectionProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.TrafficSignProperties;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.geojson.geometry.mappers.JtsLineStringJsonMapper;
import nu.ndw.nls.geojson.geometry.model.LineStringJson;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeatureBuilder {

    private final JtsLineStringJsonMapper jtsLineStringJsonMapper;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    public List<Feature> createTrafficSigns(DirectionalSegment directionalSegment,
            LongSequenceSupplier idSequenceSupplier,
            GenerateConfiguration generateConfiguration) {
        List<Feature> features = new ArrayList<>();
        addTrafficSigns(directionalSegment, idSequenceSupplier, generateConfiguration, features);
        return features;

    }

    public List<Feature> createLineStringsAndTrafficSigns(
            DirectionalSegment directionalSegment,
            LongSequenceSupplier idSequenceSupplier,
            GenerateConfiguration generateConfiguration) {

        List<Feature> features = new ArrayList<>();

        addRoadSections(directionalSegment, idSequenceSupplier, generateConfiguration, features);
        addTrafficSigns(directionalSegment, idSequenceSupplier, generateConfiguration, features);

        return features;
    }

    public Feature createPolygon(
            Geometry polygonGeometry,
            LongSequenceSupplier idSequenceSupplier,
            List<TrafficSign> relevantTrafficSigns,
            Set<Long> relevantRoadSectionIds) {

        return Feature.builder()
                .id(idSequenceSupplier.next())
                .geometry(PolygonGeometry.builder()
                        .coordinates(List.of(convertToListOfCoordinates(polygonGeometry.getCoordinates())))
                        .build())
                .properties(PolygonProperties.builder()
                        .inAccessibleRoadSectionIds(relevantRoadSectionIds.stream().sorted().toList())
                        .windowTimes(relevantTrafficSigns.stream()
                                .map(TrafficSign::findFirstTimeWindowedSign)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(TextSign::text)
                                .distinct()
                                .toList()
                        )
                        .build())
                .build();
    }

    private void addTrafficSigns(
            DirectionalSegment directionalSegment,
            LongSequenceSupplier idSequenceSupplier,
            GenerateConfiguration generateConfiguration,
            List<Feature> features) {

        if (directionalSegment.hasTrafficSigns()) {
            if (generateConfiguration.addTrafficSignsAsLineStrings()) {
                features.addAll(buildTrafficSignsAsLineString(
                        idSequenceSupplier,
                        directionalSegment.getTrafficSigns(),
                        directionalSegment,
                        generateConfiguration.trafficSignLineStringDistanceInMeters()));
            }
            if (generateConfiguration.addTrafficSignsAsPoints()) {
                features.addAll(buildTrafficSignsAsPoint(
                        idSequenceSupplier,
                        directionalSegment.getTrafficSigns(),
                        directionalSegment));
            }
        }
    }

    private void addRoadSections(
            DirectionalSegment directionalSegment,
            LongSequenceSupplier idSequenceSupplier,
            GenerateConfiguration generateConfiguration,
            List<Feature> features) {

        if (generateConfiguration.addAllRoadSectionFragments()) {
            features.add(buildRoadSection(directionalSegment, idSequenceSupplier, false));
        } else {
            if (generateConfiguration.addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections()
                    && directionalSegment.getRoadSectionFragment().isNotAccessibleFromAllSegments()) {
                features.add(buildRoadSection(directionalSegment, idSequenceSupplier, false));
            } else if (generateConfiguration.addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections()
                    && directionalSegment.getRoadSectionFragment().isAccessibleFromAllSegments()) {
                features.add(buildRoadSection(directionalSegment, idSequenceSupplier, false));
            } else if (directionalSegment.getRoadSectionFragment().isPartiallyAccessible()) {
                if (generateConfiguration.writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible()) {
                    features.add(buildRoadSection(directionalSegment, idSequenceSupplier, true));
                } else {
                    features.add(buildRoadSection(directionalSegment, idSequenceSupplier, false));
                }
            }
        }
    }

    private List<List<Double>> convertToListOfCoordinates(Coordinate[] coordinates) {

        return Arrays.stream(coordinates)
                .map(coordinate -> List.of(coordinate.x, coordinate.y))
                .toList();
    }

    private List<Feature> buildTrafficSignsAsPoint(
            LongSequenceSupplier geoJsonIdSequenceSupplier,
            List<TrafficSign> trafficSigns,
            DirectionalSegment directionalSegment) {

        LineStringJson directionSegmentLineStringJson = jtsLineStringJsonMapper.map(
                directionalSegment.getLineString());
        return trafficSigns.stream().map(trafficSign -> Feature.builder()
                .id(geoJsonIdSequenceSupplier.next())
                .geometry(PointGeometry
                        .builder()
                        .coordinates(directionSegmentLineStringJson.getCoordinates().getFirst())
                        .build())
                .properties(buildTrafficSignProperties(trafficSign, directionalSegment))
                .build()).toList();
    }

    private List<Feature> buildTrafficSignsAsLineString(
            LongSequenceSupplier geoJsonIdSequenceSupplier,
            List<TrafficSign> trafficSigns,
            DirectionalSegment directionalSegment,
            int trafficSignLineStringDistanceInMeters) {
        return trafficSigns.stream()
                .map(trafficSign -> Feature.builder()
                        .id(geoJsonIdSequenceSupplier.next())
                        .geometry(LineStringGeometry
                                .builder()
                                .coordinates(jtsLineStringJsonMapper.map(
                                        fractionAndDistanceCalculator.getSubLineStringByLengthInMeters(
                                                directionalSegment.getLineString(),
                                                trafficSignLineStringDistanceInMeters)).getCoordinates())
                                .build())
                        .properties(buildTrafficSignProperties(trafficSign, directionalSegment))
                        .build())
                .toList();

    }

    private TrafficSignProperties buildTrafficSignProperties(
            TrafficSign trafficSign,
            DirectionalSegment directionalSegment) {
        return TrafficSignProperties
                .builder()
                .trafficSignId(trafficSign.externalId())
                .nwbRoadSectionId(directionalSegment.getRoadSectionFragment().getRoadSection().getId())
                .direction(trafficSign.direction())
                .accessible(directionalSegment.isAccessible())
                .trafficSignType(trafficSign.trafficSignType())
                .windowTimes(trafficSign.findFirstTimeWindowedSign()
                        .map(TextSign::text)
                        .orElse(null))
                .iconUrl(trafficSign.iconUri())
                .isTrafficSign(true)
                .build();
    }

    private Feature buildRoadSection(
            DirectionalSegment directionalSegment,
            LongSequenceSupplier geoJsonIdSequenceSupplier,
            boolean overrideAccessibilityAsAccessible) {

        return Feature.builder()
                .id(geoJsonIdSequenceSupplier.next())
                .geometry(LineStringGeometry
                        .builder()
                        .coordinates(jtsLineStringJsonMapper.map(directionalSegment.getLineString()).getCoordinates())
                        .build())
                .properties(RoadSectionProperties
                        .builder()
                        .roadSectionFragmentId(directionalSegment.getRoadSectionFragment().getId())
                        .nwbRoadSectionId(directionalSegment.getRoadSectionFragment().getRoadSection().getId())
                        .direction(directionalSegment.getDirection())
                        .accessible(overrideAccessibilityAsAccessible || directionalSegment.isAccessible())
                        .build())
                .build();
    }
}
