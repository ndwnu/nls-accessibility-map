package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionCombinatorTest {

    private RoadSectionCombinator roadSectionCombinator;

    @BeforeEach
    void setUp() {

        roadSectionCombinator = new RoadSectionCombinator();
    }

    @Test
    void testCombineNoRestrictionsWithAccessibilityRestrictions() {

        List<RoadSection> roadSectionsWithoutRestrictions = List.of(
                buildRoadSection(id -> true)
        );

        //Adding a DirectionalSegment that does not exist in roadSectionsWithRestrictions
        addRoadSectionFragmentThatIsAccessible(roadSectionsWithoutRestrictions.getFirst());

        List<RoadSection> roadSectionsWithRestrictions = List.of(
                buildRoadSection(id -> id % 2 == 0)
        );

        Collection<RoadSection> combinedRoadSections = roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                roadSectionsWithoutRestrictions,
                roadSectionsWithRestrictions
        );

        assertThat(combinedRoadSections)
                .hasSize(1)
                .doesNotContainAnyElementsOf(roadSectionsWithoutRestrictions)
                .doesNotContainAnyElementsOf(roadSectionsWithRestrictions);

        RoadSection roadSection = combinedRoadSections.stream().findFirst().get();
        assertThat(roadSection.getId()).isEqualTo(1);

        assertThat(roadSection.getRoadSectionFragments())
                .hasSize(3)
                .doesNotContainAnyElementsOf(roadSectionsWithoutRestrictions.getFirst().getRoadSectionFragments())

                .doesNotContainAnyElementsOf(roadSectionsWithRestrictions.getFirst().getRoadSectionFragments());

        verifyRoadSectionFragment(
                roadSection.getRoadSectionFragments().getFirst(),
                0,
                roadSection,
                roadSectionsWithoutRestrictions,
                roadSectionsWithRestrictions);

        verifyRoadSectionFragment(
                roadSection.getRoadSectionFragments().get(1),
                1,
                roadSection,
                roadSectionsWithoutRestrictions,
                roadSectionsWithRestrictions);

        verifyRoadSectionFragment(
                roadSection.getRoadSectionFragments().get(2),
                Integer.MAX_VALUE,
                roadSection,
                roadSectionsWithoutRestrictions,
                roadSectionsWithRestrictions);
    }

    private void verifyRoadSectionFragment(
            RoadSectionFragment roadSectionFragment,
            int expectedRoadSectionFragmentId,
            RoadSection expectedRoadSection,
            List<RoadSection> roadSectionsWithoutRestrictions,
            List<RoadSection> roadSectionsWithRestrictions) {

        assertThat(roadSectionFragment.getId()).isEqualTo(expectedRoadSectionFragmentId);
        assertThat(roadSectionFragment.getRoadSection()).isEqualTo(expectedRoadSection);

        roadSectionFragment.getSegments().forEach(directionalSegment -> {
            verifyDirection(
                    directionalSegment,
                    roadSectionFragment,
                    roadSectionsWithoutRestrictions,
                    roadSectionsWithRestrictions);
        });
    }

    private void addRoadSectionFragmentThatIsAccessible(RoadSection roadSection) {

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .id(Integer.MAX_VALUE)
                .roadSection(roadSection)
                .build();

        DirectionalSegment directionalSegment = DirectionalSegment.builder()
                .id(Integer.MAX_VALUE)
                .direction(Direction.FORWARD)
                .lineString(mock(LineString.class))
                .trafficSign(mock(TrafficSign.class))
                .roadSectionFragment(roadSectionFragment)
                .accessible(true)
                .build();

        roadSectionFragment.setForwardSegment(directionalSegment);
        roadSection.getRoadSectionFragments().add(roadSectionFragment);
    }

    private void verifyDirection(
            DirectionalSegment directionalSegment,
            RoadSectionFragment roadSectionFragment,
            List<RoadSection> roadSectionsWithoutRestrictions,
            List<RoadSection> roadSectionsWithRestrictions) {

        DirectionalSegment expectedDirection = findComparableDirectionSegmentFromList(
                roadSectionsWithoutRestrictions,
                directionalSegment.getId());

        assertThat(directionalSegment.getId()).isEqualTo(expectedDirection.getId());
        assertThat(directionalSegment.getLineString()).isEqualTo(expectedDirection.getLineString());
        assertThat(directionalSegment.getTrafficSign()).isEqualTo(expectedDirection.getTrafficSign());
        assertThat(directionalSegment.getRoadSectionFragment()).isEqualTo(roadSectionFragment);

        verifyDirectionIsNotInCollection(directionalSegment, roadSectionsWithoutRestrictions);
        verifyDirectionIsNotInCollection(directionalSegment, roadSectionsWithRestrictions);

        if (roadSectionFragment.getForwardSegment() == directionalSegment) {
            assertThat(directionalSegment.getDirection()).isEqualTo(Direction.FORWARD);
        } else {
            assertThat(directionalSegment.getDirection()).isEqualTo(Direction.BACKWARD);
        }

        if (directionalSegment.getId() % 2 == 0) {
            assertThat(directionalSegment.isAccessible()).isTrue();
        } else {
            assertThat(directionalSegment.isAccessible()).isFalse();
        }
    }

    private DirectionalSegment findComparableDirectionSegmentFromList(
            List<RoadSection> roadSections,
            int directionSegmentId) {

        return roadSections.stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(directionalSegment -> directionalSegment.getId() == directionSegmentId)
                .findFirst()
                .orElse(null);

    }

    private void verifyDirectionIsNotInCollection(
            DirectionalSegment directionalSegmentNotExpected,
            List<RoadSection> roadSections) {

        assertThat(roadSections.stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .allMatch(directionalSegment -> directionalSegment != directionalSegmentNotExpected))
                .isTrue();
    }

    private RoadSection buildRoadSection(Function<Integer, Boolean> accessibleSupplier) {

        RoadSection roadsection = RoadSection.builder()
                .id(1L)
                .build();

        IntStream.range(0, 2)
                .forEach(roadSectionFragmentId -> {
                    RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                            .id(roadSectionFragmentId)
                            .roadSection(roadsection)
                            .build();
                    roadsection.getRoadSectionFragments().add(roadSectionFragment);

                    roadSectionFragment.setForwardSegment(
                            DirectionalSegment.builder()
                                    .id(roadSectionFragmentId + 10)
                                    .direction(Direction.FORWARD)
                                    .lineString(mock(LineString.class))
                                    .trafficSign(mock(TrafficSign.class))
                                    .roadSectionFragment(roadSectionFragment)
                                    .accessible(accessibleSupplier.apply(roadSectionFragmentId))
                                    .build());

                    roadSectionFragment.setBackwardSegment(
                            DirectionalSegment.builder()
                                    .id(roadSectionFragmentId + 20)
                                    .direction(Direction.BACKWARD)
                                    .lineString(mock(LineString.class))
                                    .trafficSign(mock(TrafficSign.class))
                                    .roadSectionFragment(roadSectionFragment)
                                    .accessible(accessibleSupplier.apply(roadSectionFragmentId))
                                    .build());
                });

        return roadsection;
    }
}