package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.trafficsign.TrafficSign;
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
        roadSectionsWithoutRestrictions.getFirst().getRoadSectionFragments().getFirst().getForwardSegments().add(
                DirectionalSegment.builder()
                        .id(Integer.MAX_VALUE)
                        .direction(Direction.FORWARD)
                        .lineString(mock(LineString.class))
                        .trafficSign(mock(TrafficSign.class))
                        .roadSectionFragment(roadSectionsWithoutRestrictions.getFirst().getRoadSectionFragments().getFirst())
                        .accessible(true)
                        .build()
        );

        List<RoadSection> roadSectionsWithRestrictions = List.of(
                buildRoadSection(id -> id % 2 == 0)
        );

        Collection<RoadSection> combinedRoadSections = roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                roadSectionsWithoutRestrictions,
                roadSectionsWithRestrictions
        );

        assertThat(combinedRoadSections).hasSize(1);
        assertThat(combinedRoadSections).doesNotContainAnyElementsOf(roadSectionsWithoutRestrictions);
        assertThat(combinedRoadSections).doesNotContainAnyElementsOf(roadSectionsWithRestrictions);

        RoadSection roadSection = combinedRoadSections.stream().findFirst().get();
        assertThat(roadSection.getId()).isEqualTo(1);

        assertThat(roadSection.getRoadSectionFragments()).hasSize(1);
        assertThat(roadSection.getRoadSectionFragments())
                .doesNotContainAnyElementsOf(roadSectionsWithoutRestrictions.getFirst().getRoadSectionFragments());
        assertThat(roadSection.getRoadSectionFragments())
                .doesNotContainAnyElementsOf(roadSectionsWithRestrictions.getFirst().getRoadSectionFragments());

        RoadSectionFragment roadSectionFragment = roadSection.getRoadSectionFragments().getFirst();
        assertThat(roadSectionFragment.getId()).isEqualTo(1);
        assertThat(roadSectionFragment.getRoadSection()).isEqualTo(roadSection);

        assertThat(roadSectionFragment.getForwardSegments()).hasSize(3);
        assertThat(roadSectionFragment.getBackwardSegments()).hasSize(2);

        roadSectionFragment.getSegments().forEach(directionalSegment -> {
            verifyDirection(
                    directionalSegment,
                    roadSectionFragment,
                    roadSectionsWithoutRestrictions,
                    roadSectionsWithRestrictions);
        });
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

        if (roadSectionFragment.getForwardSegments().contains(directionalSegment)) {
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
                .id(1)
                .build();

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .id(1)
                .roadSection(roadsection)
                .build();
        roadsection.getRoadSectionFragments().add(roadSectionFragment);

        roadSectionFragment.getForwardSegments().addAll(
                IntStream.range(0, 2)
                        .mapToObj(id -> DirectionalSegment.builder()
                                .id(id)
                                .direction(Direction.FORWARD)
                                .lineString(mock(LineString.class))
                                .trafficSign(mock(TrafficSign.class))
                                .roadSectionFragment(roadSectionFragment)
                                .accessible(accessibleSupplier.apply(id))
                                .build())
                        .toList());

        roadSectionFragment.getBackwardSegments().addAll(
                IntStream.range(2, 2 * 2)
                        .mapToObj(id -> DirectionalSegment.builder()
                                .id(id)
                                .direction(Direction.BACKWARD)
                                .lineString(mock(LineString.class))
                                .trafficSign(mock(TrafficSign.class))
                                .roadSectionFragment(roadSectionFragment)
                                .accessible(accessibleSupplier.apply(id))
                                .build())
                        .toList());

        return roadsection;
    }
}