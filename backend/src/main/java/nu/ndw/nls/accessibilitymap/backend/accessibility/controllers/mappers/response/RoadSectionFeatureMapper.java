package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionPropertiesJson;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoadSectionFeatureMapper {

    private final GeoJsonLineStringMergeMapper geoJsonLineStringMergeMapper;

    public List<RoadSectionFeatureJson> map(
            RoadSection roadSection,
            boolean includePropertyMatched,
            @Nullable CandidateMatch startPoint,
            Boolean filterOutWithAccessibility) {

        List<RoadSectionFeatureJson> features = new ArrayList<>();

        boolean isRoadSectionMatchingStartPoint = isRoadSectionMatchingStartPoint(roadSection.getId(), startPoint, true);
        if (roadSection.hasForwardSegments()) {
            if (Objects.isNull(filterOutWithAccessibility)
                    || filterOutWithAccessibility.equals(roadSection.isForwardAccessible())
                    || isRoadSectionMatchingStartPoint) {
                features.add(
                        buildRoadSectionFeature(
                                roadSection.getId(),
                                roadSection.getForwardGeometries(),
                                roadSection.isForwardAccessible(),
                                includePropertyMatched ? isRoadSectionMatchingStartPoint : null));
            }
        }

        if (roadSection.hasBackwardSegments()) {
            if (Objects.isNull(filterOutWithAccessibility)
                    || filterOutWithAccessibility.equals(roadSection.isBackwardAccessible())) {
                features.add(
                        buildRoadSectionFeature(
                                -roadSection.getId(),
                                roadSection.getBackwardGeometries(),
                                roadSection.isBackwardAccessible(),
                                includePropertyMatched
                                        ? isRoadSectionMatchingStartPoint(roadSection.getId(), startPoint, false)
                                        : null));
            }
        }

        return features;
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
            @Nullable CandidateMatch startPoint,
            boolean forward) {

        if (Objects.isNull(startPoint)) {
            return false;
        }

        return roadSectionId == startPoint.getMatchedLinkId() && forward != startPoint.isReversed();
    }

}
