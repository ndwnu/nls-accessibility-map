package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityResponseJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.DestinationJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.DirectionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RoadSectionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RoadSectionSegmentJson;
import nu.ndw.nls.geojson.geometry.model.GeometryJson.TypeEnum;
import nu.ndw.nls.geojson.geometry.model.LineStringJson;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityResponseMapperV2 {

    private final AccessibilityReasonsJsonMapperV2 accessibilityReasonsJsonMapperV2;

    public AccessibilityResponseJson map(AccessibilityRequestJson accessibilityRequest, Accessibility accessibility) {

        return AccessibilityResponseJson.builder()
                .roadSections(mapRoadSections(accessibilityRequest, accessibility))
                .destination(mapDestination(accessibility))
                .build();
    }

    private DestinationJson mapDestination(Accessibility accessibility) {
        return accessibility.toRoadSection()
                .map(roadSection -> DestinationJson.builder()
                        .roadSectionId(roadSection.getId())
                        .accessible(roadSection.isAccessibleInAnyDirection())
                        .reasons(accessibilityReasonsJsonMapperV2.map(accessibility.reasons()))
                        .build())
                .orElse(null);
    }

    private static List<RoadSectionJson> mapRoadSections(
            AccessibilityRequestJson accessibilityRequest,
            Accessibility accessibility) {

        boolean includeAccessibleRoadSections = Objects.isNull(accessibilityRequest.getIncludeAccessibleRoadSections())
                                                || accessibilityRequest.getIncludeAccessibleRoadSections();
        boolean includeInAccessibleRoadSections = Objects.isNull(accessibilityRequest.getIncludeInaccessibleRoadSections())
                                                  || accessibilityRequest.getIncludeInaccessibleRoadSections();

        return accessibility.combinedAccessibility().stream()
                .filter(roadSection ->
                        (includeAccessibleRoadSections && roadSection.isAccessibleInAnyDirection())
                        || (includeInAccessibleRoadSections && roadSection.isRestrictedInAnyDirection()))
                .map(AccessibilityResponseMapperV2::mapToRoadSection)
                .toList();
    }

    private static RoadSectionJson mapToRoadSection(RoadSection roadSection) {
        return RoadSectionJson
                .builder()
                .id(roadSection.getId())
                .roadSectionSegments(roadSection.getRoadSectionFragments().stream()
                        .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                        .map(segment -> RoadSectionSegmentJson.builder()
                                .accessible(segment.isAccessible())
                                .direction(DirectionJson.valueOf(segment.getDirection().name().toUpperCase(Locale.ROOT)))
                                .geometry(mapLineString(segment.getLineString()))
                                .build())
                        .map(RoadSectionSegmentJson.class::cast)
                        .toList())
                .build();
    }

    private static LineStringJson mapLineString(LineString lineString) {
        List<List<Double>> coordinates = Arrays.stream(lineString.getCoordinates())
                .map(coordinate -> List.of(coordinate.getX(), coordinate.getY()))
                .toList();
        return new LineStringJson(coordinates, TypeEnum.LINE_STRING);
    }
}
