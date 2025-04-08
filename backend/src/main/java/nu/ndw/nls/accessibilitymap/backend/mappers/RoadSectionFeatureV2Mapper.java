package nu.ndw.nls.accessibilitymap.backend.mappers;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionPropertiesJson;
import nu.ndw.nls.geojson.geometry.mappers.JtsLineStringJsonMapper;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RoadSectionFeatureV2Mapper {


    private static final Predicate<RoadSection> NO_FILTER = r -> true;
    private final static Predicate<RoadSection> FORWARD_ACCESSIBLE_FILTER = RoadSection::isForwardAccessible;
    private final static Predicate<RoadSection> BACKWARD_ACCESSIBLE_FILTER = RoadSection::isBackwardAccessible;
    private final static Predicate<RoadSection> BACKWARD_INACCESSIBLE_FILTER = r -> !r.isBackwardAccessible();
    private final static Predicate<RoadSection> FORWARD_INACCESSIBLE_FILTER = r -> !r.isForwardAccessible();

    private final JtsLineStringJsonMapper jtsLineStringJsonMapper;

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
            @Nullable Boolean accessible
    ) {
        List<RoadSectionFeatureJson> features = new ArrayList<>();

        if (roadSection.hasForwardSegments()) {
            boolean matchesAccessibleFilter = getForwardFilter(accessible).test(roadSection);
            boolean isMatched = mapMatched(roadSection, startPointRequested, startPointMatch, false) != null;
            if (isMatched || matchesAccessibleFilter) {
                features.add(new RoadSectionFeatureJson(
                        RoadSectionFeatureJson.TypeEnum.FEATURE,
                        Math.toIntExact(roadSection.getId()),
                        jtsLineStringJsonMapper.map(roadSection.getForwardGeometry()),
                        new RoadSectionPropertiesJson(roadSection.isForwardAccessible(),
                                mapMatched(roadSection, startPointRequested, startPointMatch, true))));
            }
        }
        if (roadSection.hasBackwardSegments()) {
            boolean matchesAccessibleFilter = getBackwardFilter(accessible).test(roadSection);
            boolean isMatched = mapMatched(roadSection, startPointRequested, startPointMatch, false) != null;
            if (isMatched || matchesAccessibleFilter) {
                features.add(new RoadSectionFeatureJson(
                        RoadSectionFeatureJson.TypeEnum.FEATURE,
                        Math.toIntExact(-roadSection.getId()),
                        jtsLineStringJsonMapper.map(roadSection.getBackWardGeometry()),
                        new RoadSectionPropertiesJson(roadSection.isBackwardAccessible(),
                                mapMatched(roadSection, startPointRequested, startPointMatch, false))));
            }
        }

        return features;
    }

    private static @Nullable Boolean mapMatched(
            RoadSection roadSection,
            boolean startPointPresent,
            @Nullable CandidateMatch startPointMatch,
            boolean forward
    ) {
        if (!startPointPresent) {
            return null;
        }

        if (startPointMatch == null) {
            return false;
        }

        return roadSection.getId() == startPointMatch.getMatchedLinkId() && forward != startPointMatch.isReversed();
    }

    private static Predicate<RoadSection> getForwardFilter(Boolean accessible) {
        return accessible == null ? NO_FILTER : accessible ? FORWARD_ACCESSIBLE_FILTER : FORWARD_INACCESSIBLE_FILTER;
    }

    private static Predicate<RoadSection> getBackwardFilter(Boolean accessible) {
        return accessible == null ? NO_FILTER : accessible ? BACKWARD_ACCESSIBLE_FILTER : BACKWARD_INACCESSIBLE_FILTER;
    }

}
