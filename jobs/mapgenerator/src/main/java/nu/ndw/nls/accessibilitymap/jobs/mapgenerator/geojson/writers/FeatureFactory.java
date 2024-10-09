package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.LineStringGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.PointGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.RoadSectionProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.TrafficSignProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.util.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeatureFactory {

    private final GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @NotNull
    public List<Feature> createFeaturesForDirectionalSegment(
            DirectionalSegment directionalSegment,
            LongSequenceSupplier idSequenceSupplier,
            GenerateConfiguration generateConfiguration) {

        List<Feature> features = new ArrayList<>();

        addRoadSections(directionalSegment, idSequenceSupplier, generateConfiguration, features);
        addTrafficSigns(directionalSegment, idSequenceSupplier, generateConfiguration, features);

        return features;
    }

    private void addTrafficSigns(
            DirectionalSegment directionalSegment,
            LongSequenceSupplier idSequenceSupplier,
            GenerateConfiguration generateConfiguration,
            List<Feature> features) {

        if (directionalSegment.hasTrafficSign()) {
            if (generateConfiguration.addTrafficSignsAsLineStrings()) {
                features.add(buildTrafficSignAsLineString(
                        idSequenceSupplier,
                        directionalSegment.getTrafficSign(),
                        directionalSegment,
                        generateConfiguration.trafficSignLineStringDistanceInMeters()));
            }
            if (generateConfiguration.addTrafficSignsAsPoints()) {
                features.add(buildTrafficSignAsPoint(
                        idSequenceSupplier,
                        directionalSegment.getTrafficSign(),
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
            } else if (generateConfiguration.writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible()
                    && directionalSegment.getRoadSectionFragment().isPartiallyAccessible()) {
                features.add(buildRoadSection(directionalSegment, idSequenceSupplier, true));
            } else {
                features.add(buildRoadSection(directionalSegment, idSequenceSupplier, false));
            }
        }
    }

    private Feature buildTrafficSignAsPoint(
            LongSequenceSupplier geoJsonIdSequenceSupplier,
            TrafficSign trafficSign,
            DirectionalSegment directionalSegment) {

        return Feature.builder()
                .id(geoJsonIdSequenceSupplier.next())
                .geometry(PointGeometry
                        .builder()
                        .coordinates(List.of(
                                directionalSegment.getLineString().getStartPoint().getX(),
                                directionalSegment.getLineString().getStartPoint().getY()))
                        .build())
                .properties(buildTrafficSignProperties(trafficSign, directionalSegment))
                .build();
    }

    private Feature buildTrafficSignAsLineString(
            LongSequenceSupplier geoJsonIdSequenceSupplier,
            TrafficSign trafficSign,
            DirectionalSegment directionalSegment,
            int trafficSignLineStringDistanceInMeters) {

        return Feature.builder()
                .id(geoJsonIdSequenceSupplier.next())
                .geometry(LineStringGeometry
                        .builder()
                        .coordinates(geoJsonLineStringCoordinateMapper.map(
                                fractionAndDistanceCalculator.getSubLineStringByLengthInMeters(
                                        directionalSegment.getLineString(),
                                        trafficSignLineStringDistanceInMeters)))
                        .build())
                .properties(buildTrafficSignProperties(trafficSign, directionalSegment))
                .build();
    }

    private TrafficSignProperties buildTrafficSignProperties(
            TrafficSign trafficSign,
            DirectionalSegment directionalSegment) {
        return TrafficSignProperties
                .builder()
                .nwbRoadSectionId(trafficSign.roadSectionId())
                .direction(trafficSign.direction())
                .accessible(directionalSegment.isAccessible())
                .trafficSignType(trafficSign.trafficSignType())
                .windowTimes(trafficSign.findFirstTimeWindowedSign()
                        .map(TextSign::getText)
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
                        .coordinates(geoJsonLineStringCoordinateMapper.map(directionalSegment.getLineString()))
                        .build())
                .properties(RoadSectionProperties
                        .builder()
                        .nwbRoadSectionId(directionalSegment.getRoadSectionFragment().getRoadSection().getId())
                        .direction(directionalSegment.getDirection())
                        .accessible(overrideAccessibilityAsAccessible || directionalSegment.isAccessible())
                        .build())
                .build();
    }
}
