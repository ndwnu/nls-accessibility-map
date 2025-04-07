package nu.ndw.nls.accessibilitymap.backend.mappers;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.springframework.stereotype.Component;
import rx.Observable;

@Component
@RequiredArgsConstructor
public class RoadSectionFeatureCollectionV2Mapper {

    // accessible = null
    private static final Predicate<RoadSection> NO_FILTER = r -> true;
    // accessible true
    private final Predicate<RoadSection> FORWARD_ACCESSIBLE_FILTER = RoadSection::isForwardAccessible;
    // accessible true
    private final Predicate<RoadSection> BACKWARD_ACCESSIBLE_FILTER = RoadSection::isBackwardAccessible;

    private final Predicate<RoadSection> BACKWARD_INACCESSIBLE_FILTER = r -> !r.isBackwardAccessible();

    private final Predicate<RoadSection> FORWARD_INACCESSIBLE_FILTER = r -> !r.isForwardAccessible();

    private final RoadSectionFeatureV2Mapper roadSectionFeatureV2Mapper;


    /**
     * Maps accessibility data into a RoadSectionFeatureCollectionJson object containing road section features.
     *
     * @param accessibility       The accessibility data containing collections of road sections.
     * @param startPointRequested A boolean indicating if a starting point has been requested.
     * @param startPointMatch     The candidate match for the starting point, may be nullable.
     * @param accessible          Boolean to filter road sections based on accessibility; can be null for no filtering.
     * @return A RoadSectionFeatureCollectionJson object containing the mapped road section features.
     */
    public RoadSectionFeatureCollectionJson map(
            Accessibility accessibility,
            boolean startPointRequested,
            @Nullable CandidateMatch startPointMatch,
            @Nullable Boolean accessible
    ) {

        List<RoadSectionFeatureJson> features = new ArrayList<>();

        Observable<RoadSection> roadSectionObservable = Observable.from(accessibility.combinedAccessibility());

        Observable<RoadSection> forwardRoadSectionsObservable = roadSectionObservable
                .filter(RoadSection::hasForwardSegments);

        Observable<RoadSection> backwardRoadSectionObservable = roadSectionObservable.filter(
                RoadSection::hasBackwardSegments);

        forwardRoadSectionsObservable.subscribe(
                r -> {
                    boolean matchesAccessibleFilter = getForwardFilter(accessible).test(r);
                    boolean isMatched = mapMatched(r, startPointRequested, startPointMatch, true) != null;
                    if (isMatched || matchesAccessibleFilter) {
                        features.add(
                                roadSectionFeatureV2Mapper.map(r, mapMatched(r, startPointRequested, startPointMatch, true),
                                        true));
                    }
                });

        backwardRoadSectionObservable.subscribe(
                r -> {
                    boolean matchesAccessibleFilter = getBackwardFilter(accessible).test(r);
                    boolean isMatched = mapMatched(r, startPointRequested, startPointMatch, false) != null;
                    if (isMatched || matchesAccessibleFilter) {
                        features.add(
                                roadSectionFeatureV2Mapper.map(r, mapMatched(r, startPointRequested, startPointMatch, false),
                                        false));
                    }
                });

        return new RoadSectionFeatureCollectionJson(TypeEnum.FEATURE_COLLECTION, features);
    }

    private Predicate<RoadSection> getForwardFilter(Boolean accessible) {
        return accessible == null ? NO_FILTER : accessible ? FORWARD_ACCESSIBLE_FILTER : FORWARD_INACCESSIBLE_FILTER;
    }

    private Predicate<RoadSection> getBackwardFilter(Boolean accessible) {
        return accessible == null ? NO_FILTER : accessible ? BACKWARD_ACCESSIBLE_FILTER : BACKWARD_INACCESSIBLE_FILTER;
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
}
