package nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.writers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.dto.LineStringGeometry;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.dto.PointGeometry;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.dto.PolygonGeometry;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.dto.PolygonProperties;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.dto.RoadSectionProperties;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.dto.TrafficSignProperties;
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

    public List<Feature> createTrafficSigns(
            DirectionalSegment directionalSegment,
            AtomicLong idSequenceSupplier,
            GenerateConfiguration generateConfiguration) {
        List<Feature> features = new ArrayList<>();
        addTrafficSigns(directionalSegment, idSequenceSupplier, generateConfiguration, features);
        return features;
    }

    public List<Feature> createLineStringsAndTrafficSigns(
            DirectionalSegment directionalSegment,
            AtomicLong idSequenceSupplier,
            GenerateConfiguration generateConfiguration) {

        List<Feature> features = new ArrayList<>();

        addRoadSections(directionalSegment, idSequenceSupplier, generateConfiguration, features);
        addTrafficSigns(directionalSegment, idSequenceSupplier, generateConfiguration, features);

        return features;
    }

    public Feature createPolygon(
            Geometry polygonGeometry,
            AtomicLong idSequenceSupplier,
            Set<Restriction> relevantRestrictions,
            Set<Long> relevantRoadSectionIds) {

        return Feature.builder()
                .id(idSequenceSupplier.getAndIncrement())
                .geometry(PolygonGeometry.builder()
                        .coordinates(List.of(convertToListOfCoordinates(polygonGeometry.getCoordinates())))
                        .build())
                .properties(PolygonProperties.builder()
                        .inAccessibleRoadSectionIds(relevantRoadSectionIds.stream().sorted().toList())
                        .windowTimes(relevantRestrictions.stream()
                                //Todo: Add support for other types of restrictions
                                .filter(TrafficSign.class::isInstance)
                                .map(TrafficSign.class::cast)
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
            AtomicLong idSequenceSupplier,
            GenerateConfiguration generateConfiguration,
            List<Feature> features) {

        if (directionalSegment.hasRestrictions()) {
            if (generateConfiguration.addTrafficSignsAsLineStrings()) {
                features.addAll(buildTrafficSignsAsLineString(
                        idSequenceSupplier,
                        directionalSegment.getRestrictions(),
                        directionalSegment,
                        generateConfiguration.trafficSignLineStringDistanceInMeters()));
            }
            if (generateConfiguration.addTrafficSignsAsPoints()) {
                features.addAll(buildRestrictionAsPoint(
                        idSequenceSupplier,
                        directionalSegment.getRestrictions(),
                        directionalSegment));
            }
        }
    }

    private void addRoadSections(
            DirectionalSegment directionalSegment,
            AtomicLong idSequenceSupplier,
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

    private List<Feature> buildRestrictionAsPoint(
            AtomicLong geoJsonIdSequenceSupplier,
            List<Restriction> restrictions,
            DirectionalSegment directionalSegment) {

        LineStringJson directionSegmentLineStringJson = jtsLineStringJsonMapper.map(
                directionalSegment.getLineString());
        return restrictions.stream()
                // Todo: Add support for other restriction types
                .filter(TrafficSign.class::isInstance)
                .map(TrafficSign.class::cast)
                .map(trafficSign -> Feature.builder()
                        .id(geoJsonIdSequenceSupplier.getAndIncrement())
                        .geometry(PointGeometry
                                .builder()
                                .coordinates(directionSegmentLineStringJson.getCoordinates().getFirst())
                                .build())
                        .properties(buildTrafficSignProperties(trafficSign, directionalSegment))
                        .build()).toList();
    }

    private List<Feature> buildTrafficSignsAsLineString(
            AtomicLong geoJsonIdSequenceSupplier,
            List<Restriction> restrictions,
            DirectionalSegment directionalSegment,
            int trafficSignLineStringDistanceInMeters) {
        return restrictions.stream()
                // Todo: Add support for other restriction types
                .filter(TrafficSign.class::isInstance)
                .map(TrafficSign.class::cast)
                .map(trafficSign -> Feature.builder()
                        .id(geoJsonIdSequenceSupplier.getAndIncrement())
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
            AtomicLong geoJsonIdSequenceSupplier,
            boolean overrideAccessibilityAsAccessible) {

        return Feature.builder()
                .id(geoJsonIdSequenceSupplier.getAndIncrement())
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
