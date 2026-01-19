package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RoadSectionPropertiesJson;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoadSectionFeatureMapper {

    private final GeoJsonLineStringMergeMapper geoJsonLineStringMergeMapper;

    public List<RoadSectionFeatureJson> map(
            RoadSection roadSection,
            boolean includePropertyMatched,
            @Nullable Long matchedStartPointRoadSectionId,
            Boolean filterOutWithAccessibility) {

        List<RoadSectionFeatureJson> features = new ArrayList<>();

        boolean isRoadSectionMatchingStartPoint = isRoadSectionMatchingStartPoint(roadSection.getId(), matchedStartPointRoadSectionId);
        if (roadSection.hasForwardSegments()
            && shouldBeIncluded(roadSection.isForwardAccessible(), filterOutWithAccessibility, isRoadSectionMatchingStartPoint)) {
            features.add(
                    buildRoadSectionFeature(
                            roadSection.getId(),
                            roadSection.getForwardGeometries(),
                            roadSection.isForwardAccessible(),
                            includePropertyMatched ? isRoadSectionMatchingStartPoint : null));
        }

        if (roadSection.hasBackwardSegments()
            && shouldBeIncluded(roadSection.isBackwardAccessible(), filterOutWithAccessibility, isRoadSectionMatchingStartPoint)) {
            features.add(
                    buildRoadSectionFeature(
                            -roadSection.getId(),
                            roadSection.getBackwardGeometries(),
                            roadSection.isBackwardAccessible(),
                            includePropertyMatched
                                    ? isRoadSectionMatchingStartPoint
                                    : null));
        }

        return features;
    }

    private static boolean shouldBeIncluded(
            boolean isAccessible,
            Boolean filterOutWithAccessibility,
            boolean isRoadSectionMatchingStartPoint) {

        return Objects.isNull(filterOutWithAccessibility)
               || filterOutWithAccessibility.equals(isAccessible)
               || isRoadSectionMatchingStartPoint;
    }

    private RoadSectionFeatureJson buildRoadSectionFeature(
            long roadSectionId,
            List<LineString> geometries,
            boolean isAccessible,
            Boolean isMatched) {

        return new RoadSectionFeatureJson(
                RoadSectionFeatureJson.TypeEnum.FEATURE,
                Math.toIntExact(roadSectionId),
                geoJsonLineStringMergeMapper.mapToLineStringJson(geometries),
                new RoadSectionPropertiesJson(isAccessible, isMatched));
    }

    private static boolean isRoadSectionMatchingStartPoint(
            long roadSectionId,
            @Nullable Long matchedStartPointRoadSectionId) {

        if (Objects.isNull(matchedStartPointRoadSectionId)) {
            return false;
        }

        return roadSectionId == matchedStartPointRoadSectionId;
    }

}
