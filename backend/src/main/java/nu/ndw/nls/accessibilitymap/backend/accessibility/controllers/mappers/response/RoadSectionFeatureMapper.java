package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
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

    private static final Predicate<RoadSection> NO_FILTER = r -> true;

    private static final Predicate<RoadSection> FORWARD_ACCESSIBLE_FILTER = RoadSection::isForwardAccessible;

    private static final Predicate<RoadSection> BACKWARD_ACCESSIBLE_FILTER = RoadSection::isBackwardAccessible;

    private static final Predicate<RoadSection> BACKWARD_INACCESSIBLE_FILTER = r -> !r.isBackwardAccessible();

    private static final Predicate<RoadSection> FORWARD_INACCESSIBLE_FILTER = r -> !r.isForwardAccessible();

    private final GeoJsonLineStringMergeMapper geoJsonLineStringMergeMapper;

    /**
     * Maps a given RoadSection to a list of RoadSectionFeatureJson objects. The mapping is based on the accessibility and match filters
     * applied to the road section.
     *
     * @param roadSection         the road section to be mapped
     * @param startPointRequested a flag indicating if the start point is to be included
     * @param startPointMatch     an optional CandidateMatch for the start point, may be null
     * @param accessible          an optional Boolean indicating the accessibility filter to apply, may be null
     * @return a list of RoadSectionFeatureJson objects representing the mapped road section
     */
    public List<RoadSectionFeatureJson> map(
            RoadSection roadSection,
            boolean startPointRequested,
            @Nullable CandidateMatch startPointMatch,
            @Nullable Boolean accessible) {

        List<RoadSectionFeatureJson> features = new ArrayList<>();

        if (roadSection.hasForwardSegments()) {
            boolean matchesAccessibleFilter = getForwardFilter(accessible).test(roadSection);
            boolean isMatched = startPointRequested && mapMatched(roadSection, startPointMatch, true);
            if (isMatched || matchesAccessibleFilter) {
                features.add(buildRoadSectionFeature(
                        roadSection.getId(),
                        roadSection.getForwardGeometries(),
                        roadSection.isForwardAccessible(),
                        isMatched));
            }
        }

        if (roadSection.hasBackwardSegments()) {
            boolean matchesAccessibleFilter = getBackwardFilter(accessible).test(roadSection);
            boolean isMatched = startPointRequested && mapMatched(roadSection, startPointMatch, false);
            if (isMatched || matchesAccessibleFilter) {
                features.add(buildRoadSectionFeature(
                        -roadSection.getId(),
                        roadSection.getBackwardGeometries(),
                        roadSection.isBackwardAccessible(),
                        isMatched));
            }
        }

        return features;
    }

    private RoadSectionFeatureJson buildRoadSectionFeature(
            long roadSection,
            List<LineString> geometries,
            boolean isRoadSectionAccessible,
            boolean isMatched) {

        return new RoadSectionFeatureJson(
                RoadSectionFeatureJson.TypeEnum.FEATURE,
                Math.toIntExact(roadSection),
                geoJsonLineStringMergeMapper.mapToLineStringJson(geometries),
                new RoadSectionPropertiesJson(isRoadSectionAccessible, isMatched));
    }

    private static boolean mapMatched(
            RoadSection roadSection,
            @Nullable CandidateMatch startPointMatch,
            boolean forward) {

        if (startPointMatch == null) {
            return false;
        }

        return roadSection.getId() == startPointMatch.getMatchedLinkId() && forward != startPointMatch.isReversed();
    }

    private static Predicate<RoadSection> getForwardFilter(Boolean accessible) {

        if (Objects.isNull(accessible)) {
            return NO_FILTER;
        } else {
            return Boolean.TRUE.equals(accessible) ? FORWARD_ACCESSIBLE_FILTER : FORWARD_INACCESSIBLE_FILTER;
        }
    }

    private static Predicate<RoadSection> getBackwardFilter(Boolean accessible) {

        if (Objects.isNull(accessible)) {
            return NO_FILTER;
        } else {
            return Boolean.TRUE.equals(accessible) ? BACKWARD_ACCESSIBLE_FILTER : BACKWARD_INACCESSIBLE_FILTER;
        }
    }

}
