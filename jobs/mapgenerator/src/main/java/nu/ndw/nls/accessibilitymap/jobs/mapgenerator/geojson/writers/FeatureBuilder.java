package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.LineStringGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.PointGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.PolygonGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.PolygonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.RoadSectionProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.TrafficSignProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.utils.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeatureBuilder {

    private final GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

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
            List<TrafficSign> relevantTrafficSigns) {
        return Feature.builder()
                .id(idSequenceSupplier.next())
                .geometry(PolygonGeometry.builder()
                        .coordinates(List.of(convertToListOfCoordinates(polygonGeometry.getCoordinates())))
                        .build())
                .properties(PolygonProperties.builder()
                        .windowTimes(relevantTrafficSigns.stream()
                                .map(TrafficSign::findFirstTimeWindowedSign)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(TextSign::getText)
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
                .nwbRoadSectionId(directionalSegment.getRoadSectionFragment().getRoadSection().getId())
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
                        .roadSectionFragmentId(directionalSegment.getRoadSectionFragment().getId())
                        .nwbRoadSectionId(directionalSegment.getRoadSectionFragment().getRoadSection().getId())
                        .direction(directionalSegment.getDirection())
                        .accessible(overrideAccessibilityAsAccessible || directionalSegment.isAccessible())
                        .build())
                .build();
    }
}
