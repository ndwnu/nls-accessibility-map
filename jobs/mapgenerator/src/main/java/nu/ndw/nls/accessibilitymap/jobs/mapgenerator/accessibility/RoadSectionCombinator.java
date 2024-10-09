package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSectionFragment;
import org.springframework.stereotype.Component;

@Component
public class RoadSectionCombinator {

    public Collection<RoadSection> combineNoRestrictionsWithAccessibilityRestrictions(
            Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
            Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions) {

        List<DirectionalSegment> allDirectionalSegments =
                accessibleRoadsSectionsWithoutAppliedRestrictions.stream()
                        .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                        .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                        .toList();

        Map<Integer, DirectionalSegment> directionalSegmentsThatAreAccessible =
                accessibleRoadSectionsWithAppliedRestrictions.stream()
                        .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                        .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                        .collect(Collectors.toMap(DirectionalSegment::getId, Function.identity()));

        SortedMap<Long, RoadSection> roadSectionsById = new TreeMap<>();
        SortedMap<Integer, RoadSectionFragment> roadSectionsFragmentsById = new TreeMap<>();

        allDirectionalSegments.forEach(
                directionalSegmentToCopyFrom -> {
                    RoadSection newRoadSection = roadSectionsById.computeIfAbsent(
                            directionalSegmentToCopyFrom.getRoadSectionFragment().getRoadSection().getId(),
                            roadSectionId -> RoadSection.builder()
                                    .id(roadSectionId)
                                    .build());

                    RoadSectionFragment newRoadSectionFraction = roadSectionsFragmentsById.computeIfAbsent(
                            directionalSegmentToCopyFrom.getRoadSectionFragment().getId(),
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
                            directionalSegmentToCopyFrom,
                            directionalSegmentsThatAreAccessible.get(directionalSegmentToCopyFrom.getId()));
                });

        return roadSectionsById.values();
    }

    private void addNewDirectionSegmentToRoadSection(
            RoadSectionFragment newRoadSectionFraction,
            DirectionalSegment directionalSegmentToCopyFrom,
            DirectionalSegment accessibleDirectionSegment) {

        DirectionalSegment newDirectionSegment = directionalSegmentToCopyFrom
                .withAccessible(
                        Objects.nonNull(accessibleDirectionSegment) && accessibleDirectionSegment.isAccessible()
                )
                .withRoadSectionFragment(newRoadSectionFraction);

        if (directionalSegmentToCopyFrom.getDirection() == Direction.BACKWARD) {
            newRoadSectionFraction.setBackwardSegment(newDirectionSegment);
        } else {
            newRoadSectionFraction.setForwardSegment(newDirectionSegment);
        }
    }
}
