package nu.ndw.nls.accessibilitymap.accessibility.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import org.springframework.stereotype.Component;

@Component
public class RoadSectionCombinator {

    public Collection<RoadSection> combineNoRestrictionsWithAccessibilityRestrictions(
            Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
            Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions) {

        List<DirectionalSegment> reachableSegmentsWhenNoRestrictionsApplied =
                accessibleRoadsSectionsWithoutAppliedRestrictions.stream()
                        .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                        .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                        .toList();

        Map<Integer, DirectionalSegment> reachableSegmentsWhenRestrictionsApplied =
                accessibleRoadSectionsWithAppliedRestrictions.stream()
                        .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                        .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                        .collect(Collectors.toMap(DirectionalSegment::getId, Function.identity()));

        SortedMap<Long, RoadSection> roadSectionsById = new TreeMap<>();
        SortedMap<Integer, RoadSectionFragment> roadSectionsFragmentsById = new TreeMap<>();

        reachableSegmentsWhenNoRestrictionsApplied.forEach(
                directionalSegmentNoRestrictionsApplied -> {
                    RoadSection newRoadSection = roadSectionsById.computeIfAbsent(
                            directionalSegmentNoRestrictionsApplied.getRoadSectionFragment().getRoadSection().getId(),
                            roadSectionId -> RoadSection.builder()
                                    .id(roadSectionId)
                                    .functionalRoadClass(directionalSegmentNoRestrictionsApplied
                                            .getRoadSectionFragment()
                                            .getRoadSection()
                                            .getFunctionalRoadClass())
                                    .build());

                    RoadSectionFragment newRoadSectionFraction = roadSectionsFragmentsById.computeIfAbsent(
                            directionalSegmentNoRestrictionsApplied.getRoadSectionFragment().getId(),
                            roadSectionFragmentId -> {
                                RoadSectionFragment newRoadSectionFragment = RoadSectionFragment.builder()
                                        .id(roadSectionFragmentId)
                                        .roadSection(newRoadSection)
                                        .build();

                                newRoadSection.getRoadSectionFragments().add(newRoadSectionFragment);
                                return newRoadSectionFragment;
                            });

                    addNewDirectionSegmentToRoadSection(
                            newRoadSectionFraction,
                            directionalSegmentNoRestrictionsApplied,
                            reachableSegmentsWhenRestrictionsApplied.get(directionalSegmentNoRestrictionsApplied.getId()));
                });

        return roadSectionsById.values();
    }

    private static void addNewDirectionSegmentToRoadSection(
            RoadSectionFragment newRoadSectionFraction,
            DirectionalSegment directionalSegmentNoRestrictionsApplied,
            DirectionalSegment directionalSegmentWithRestrictionsApplied) {

        DirectionalSegment newDirectionSegment = directionalSegmentNoRestrictionsApplied
                .withAccessible(
                        Objects.nonNull(directionalSegmentWithRestrictionsApplied)
                        && directionalSegmentWithRestrictionsApplied.isAccessible()
                )
                .withDelayBecauseOfRestrictions(
                        Objects.isNull(directionalSegmentWithRestrictionsApplied)
                                ? 0
                                : (
                                        directionalSegmentWithRestrictionsApplied.getTravelTimeInMilliSeconds()
                                        - directionalSegmentNoRestrictionsApplied.getTravelTimeInMilliSeconds()
                                ))
                .withRoadSectionFragment(newRoadSectionFraction);

        if (directionalSegmentNoRestrictionsApplied.getDirection() == Direction.BACKWARD) {
            newRoadSectionFraction.setBackwardSegment(newDirectionSegment);
        } else {
            newRoadSectionFraction.setForwardSegment(newDirectionSegment);
        }
    }
}
