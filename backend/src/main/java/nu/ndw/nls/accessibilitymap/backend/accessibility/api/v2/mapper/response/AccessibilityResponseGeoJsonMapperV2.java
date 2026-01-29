package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityResponseGeoJsonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.DestinationFeaturePropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.DestinationRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.DirectionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FeatureJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RoadSectionSegmentFeaturePropertiesJson;
import nu.ndw.nls.geojson.geometry.model.GeometryJson.TypeEnum;
import nu.ndw.nls.geojson.geometry.model.LineStringJson;
import nu.ndw.nls.geojson.geometry.model.PointJson;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityResponseGeoJsonMapperV2 {

    private final AccessibilityReasonsJsonMapperV2 accessibilityReasonsJsonMapperV2;

    public AccessibilityResponseGeoJsonJson map(AccessibilityRequestJson accessibilityRequestJson, Accessibility accessibility) {

        AtomicInteger idGenerator = new AtomicInteger();
        var roadSectionFeatures = accessibility.combinedAccessibility().stream()
                .map(roadSection -> mapToRoadSectionFeature(accessibilityRequestJson, roadSection, idGenerator))
                .flatMap(List::stream);
        var destinationFeatures = mapDestinationFeatures(accessibilityRequestJson, accessibility, idGenerator);

        return AccessibilityResponseGeoJsonJson.builder()
                .type(AccessibilityResponseGeoJsonJson.TypeEnum.FEATURE_COLLECTION)
                .features(Stream.concat(destinationFeatures, roadSectionFeatures)
                        .toList())
                .build();
    }

    private Stream<FeatureJson> mapDestinationFeatures(
            AccessibilityRequestJson accessibilityRequestJson,
            Accessibility accessibility,
            AtomicInteger idGenerator) {
        DestinationRequestJson requestDestination = accessibilityRequestJson.getDestination();
        Optional<RoadSection> toRoadSection = accessibility.toRoadSection();

        if (Objects.isNull(requestDestination) || toRoadSection.isEmpty()) {
            return Stream.empty();
        } else {
            return Stream.of(FeatureJson.builder()
                    .id(idGenerator.incrementAndGet())
                    .type(FeatureJson.TypeEnum.FEATURE)
                    .geometry(new PointJson(List.of(requestDestination.getLongitude(), requestDestination.getLatitude()), TypeEnum.POINT))
                    .properties(DestinationFeaturePropertiesJson.builder()
                            .roadSectionId(toRoadSection.get().getId())
                            .accessible(toRoadSection.get().isAccessibleInAnyDirection())
                            .reasons(accessibilityReasonsJsonMapperV2.map(accessibility.reasons()))
                            .build())
                    .build());
        }
    }

    private List<FeatureJson> mapToRoadSectionFeature(
            AccessibilityRequestJson accessibilityRequestJson,
            RoadSection roadSection,
            AtomicInteger idGenerator) {

        boolean includeAccessibleRoadSections = includeAccessibleRoadSections(accessibilityRequestJson);
        boolean includeInAccessibleRoadSections = isIncludeInAccessibleRoadSections(accessibilityRequestJson);

        return roadSection.getRoadSectionFragments().stream()
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(directionalSegment ->
                        (includeAccessibleRoadSections && isAccessible(accessibilityRequestJson, directionalSegment))
                        || (includeInAccessibleRoadSections && !isAccessible(accessibilityRequestJson, directionalSegment)))
                .map(directionalSegment -> FeatureJson.builder()
                        .id(idGenerator.incrementAndGet())
                        .type(FeatureJson.TypeEnum.FEATURE)
                        .geometry(mapLineString(directionalSegment.getLineString()))
                        .properties(RoadSectionSegmentFeaturePropertiesJson.builder()
                                .roadSectionId(roadSection.getId())
                                .accessible(isAccessible(accessibilityRequestJson, directionalSegment))
                                .direction(DirectionJson.valueOf(directionalSegment.getDirection().name().toUpperCase(Locale.ROOT)))
                                .build())
                        .build())
                .map(FeatureJson.class::cast)
                .toList();
    }

    private static boolean isIncludeInAccessibleRoadSections(AccessibilityRequestJson accessibilityRequestJson) {
        return Objects.isNull(accessibilityRequestJson.getIncludeInaccessibleRoadSections())
               || accessibilityRequestJson.getIncludeInaccessibleRoadSections();
    }

    private static boolean includeAccessibleRoadSections(AccessibilityRequestJson accessibilityRequestJson) {
        return Objects.isNull(accessibilityRequestJson.getIncludeAccessibleRoadSections())
               || accessibilityRequestJson.getIncludeAccessibleRoadSections();
    }

    private static LineStringJson mapLineString(LineString lineString) {

        List<List<Double>> coordinates = Arrays.stream(lineString.getCoordinates())
                .map(coordinate -> List.of(coordinate.getX(), coordinate.getY()))
                .toList();

        return new LineStringJson(coordinates, TypeEnum.LINE_STRING);
    }

    public static boolean isAccessible(AccessibilityRequestJson accessibilityRequestJson, DirectionalSegment directionalSegment) {
        if (Objects.isNull(accessibilityRequestJson.getEffectivelyAccessible())
            || Boolean.FALSE.equals(accessibilityRequestJson.getEffectivelyAccessible())) {
            return directionalSegment.isAccessible();
        } else {
            return directionalSegment.getRoadSectionFragment().isAccessibleFromAnySegment();
        }
    }
}
