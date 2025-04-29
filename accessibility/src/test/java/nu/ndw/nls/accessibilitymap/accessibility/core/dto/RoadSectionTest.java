package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionTest extends ValidationTest {

    private RoadSection roadSection;

    private RoadSectionFragment roadSectionFragment;

    private DirectionalSegment directionalSegmentForward;

    private DirectionalSegment directionalSegmentBackward;

    @Mock
    private LineString lineString;

    @Mock
    private LineString lineString1;

    @Mock
    private LineString lineString2;

    private final GeometryFactoryWgs84 geometryFactoryWgs84 = new GeometryFactoryWgs84();

    @BeforeEach
    void setUp() {

        roadSection = RoadSection.builder()
                .id(1L)
                .build();

        roadSectionFragment = RoadSectionFragment.builder()
                .id(2)
                .roadSection(roadSection)
                .build();
        roadSection.getRoadSectionFragments().add(roadSectionFragment);

        directionalSegmentForward = DirectionalSegment.builder()
                .id(3)
                .accessible(true)
                .direction(Direction.FORWARD)
                .roadSectionFragment(roadSectionFragment)
                .lineString(lineString)
                .trafficSigns(List.of(TrafficSign.builder()
                        .id(4)
                        .roadSectionId(1)
                        .externalId("externalId")
                        .direction(Direction.FORWARD)
                        .fraction(2d)
                        .longitude(3d)
                        .latitude(4d)
                        .textSigns(List.of())
                        .networkSnappedLatitude(1D)
                        .networkSnappedLongitude(2D)
                        .trafficSignType(TrafficSignType.C7)
                        .restrictions(Restrictions.builder().build())
                        .build()))
                .build();

        directionalSegmentBackward = directionalSegmentForward.withDirection(Direction.BACKWARD);

        roadSectionFragment.setForwardSegment(directionalSegmentForward);
        roadSectionFragment.setBackwardSegment(directionalSegmentBackward);
    }

    @Test
    void copy() {

        LineString lineString = geometryFactoryWgs84.createLineString();
        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment
                .toBuilder()
                .forwardSegment(directionalSegmentForward.withLineString(lineString))
                .backwardSegment(directionalSegmentBackward.withLineString(lineString))
                .build()));

        RoadSection copiedRoadSection = roadSection.copy();
        assertThat(copiedRoadSection).isNotSameAs(roadSection);
        assertThat(copiedRoadSection.getId()).isEqualTo(roadSection.getId());

        for (int i = 0; i < roadSection.getRoadSectionFragments().size(); i++) {
            RoadSectionFragment roadSectionFragment = roadSection.getRoadSectionFragments().get(i);
            RoadSectionFragment copiedRoadSectionFragment = copiedRoadSection.getRoadSectionFragments().get(i);

            assertThat(copiedRoadSectionFragment).isNotSameAs(roadSectionFragment);
            assertThat(copiedRoadSectionFragment.getId()).isEqualTo(roadSectionFragment.getId());
            DirectionalSegment copiedForwardSegment = copiedRoadSectionFragment.getForwardSegment();
            DirectionalSegment copiedBackwardSegment = copiedRoadSectionFragment.getBackwardSegment();
            assertThat(copiedForwardSegment).isNotSameAs(roadSectionFragment.getForwardSegment());
            assertThat(copiedBackwardSegment).isNotSameAs(roadSectionFragment.getBackwardSegment());
            assertThat(copiedForwardSegment.getId()).isEqualTo(roadSectionFragment.getForwardSegment().getId());
            assertThat(copiedBackwardSegment.getId()).isEqualTo(roadSectionFragment.getBackwardSegment().getId());
            // enums are fixed reference types
            assertThat(copiedForwardSegment.getDirection()).isEqualTo(roadSectionFragment.getForwardSegment().getDirection());
            assertThat(copiedBackwardSegment.getDirection()).isEqualTo(roadSectionFragment.getBackwardSegment().getDirection());
            assertThat(copiedForwardSegment.getLineString()).isEqualTo(roadSectionFragment.getForwardSegment().getLineString());
            assertThat(copiedForwardSegment.getLineString()).isNotSameAs(roadSectionFragment.getForwardSegment().getLineString());
            assertThat(copiedBackwardSegment.getLineString()).isEqualTo(roadSectionFragment.getBackwardSegment().getLineString());
            assertThat(copiedBackwardSegment.getLineString()).isNotSameAs(roadSectionFragment.getBackwardSegment().getLineString());
        }
    }

    @Test
    void hasForwardSegments_whenNoSegments_shouldReturnFalse() {
        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment
                .toBuilder().forwardSegment(null)
                .build()));
        boolean result = roadSection.hasForwardSegments();
        assertThat(result).isFalse();
    }

    @Test
    void hasBackwardSegments_whenNoSegments_shouldReturnFalse() {
        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment
                .toBuilder().backwardSegment(null)
                .build()));
        boolean result = roadSection.hasBackwardSegments();
        assertThat(result).isFalse();
    }

    @Test
    void isRestrictedInAnyDirection_whenNoRestrictions_shouldReturnFalse() {
        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment
                .toBuilder()
                .forwardSegment(directionalSegmentForward.withAccessible(true))
                .backwardSegment(directionalSegmentBackward.withAccessible(true))
                .build()));

        boolean result = roadSection.isRestrictedInAnyDirection();
        assertThat(result).isFalse();
    }

    @Test
    void isRestrictedInAnyDirection_whenRestrictionsExist_shouldReturnTrue() {
        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment
                .toBuilder()
                .forwardSegment(directionalSegmentForward.withAccessible(false))
                .backwardSegment(directionalSegmentBackward.withAccessible(true))
                .build()));

        boolean result = roadSection.isRestrictedInAnyDirection();

        assertThat(result).isTrue();
    }

    @Test
    void isRestrictedInAnyDirection_whenRestrictionsExist_shouldReturnTrue_whenBothDirectionsAreInAccessible() {
        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment
                .toBuilder()
                .forwardSegment(directionalSegmentForward.withAccessible(false))
                .backwardSegment(directionalSegmentBackward.withAccessible(false))
                .build()));
        boolean result = roadSection.isRestrictedInAnyDirection();
        assertThat(result).isTrue();
    }

    @Test
    void isForwardAccessible_whenNoSegments_shouldReturnFalse() {
        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment
                .toBuilder().forwardSegment(null)
                .build()));
        boolean result = roadSection.isForwardAccessible();
        assertThat(result).isFalse();
    }

    @Test
    void isForwardAccessible_whenOneIsInaccessible_shouldReturnFalse() {
        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment
                        .toBuilder()
                        .forwardSegment(directionalSegmentForward.withAccessible(true))
                        .build(),
                roadSectionFragment
                        .toBuilder()
                        .forwardSegment(directionalSegmentForward.withAccessible(false))
                        .build()
        ));
        boolean result = roadSection.isForwardAccessible();
        assertThat(result).isFalse();
    }

    @Test
    void isForwardAccessible_whenAllAreAccessible_shouldReturnTrue() {
        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment
                        .toBuilder()
                        .forwardSegment(directionalSegmentForward.withAccessible(true))
                        .build(),
                roadSectionFragment
                        .toBuilder()
                        .forwardSegment(directionalSegmentForward.withAccessible(true))
                        .build()
        ));
        boolean result = roadSection.isForwardAccessible();
        assertThat(result).isTrue();
    }

    @Test
    void isBackwardAccessible_whenNoSegments_shouldReturnFalse() {
        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment
                .toBuilder().backwardSegment(null)
                .build()));
        boolean result = roadSection.isBackwardAccessible();
        assertThat(result).isFalse();
    }

    @Test
    void isBackwardAccessible_whenOneIsInaccessible_shouldReturnFalse() {
        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment
                        .toBuilder()
                        .backwardSegment(directionalSegmentBackward.withAccessible(true))
                        .build(),
                roadSectionFragment
                        .toBuilder()
                        .backwardSegment(directionalSegmentBackward.withAccessible(false))
                        .build()
        ));
        boolean result = roadSection.isBackwardAccessible();
        assertThat(result).isFalse();
    }

    @Test
    void isBackwardAccessible_whenAllAreAccessible_shouldReturnTrue() {
        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment
                        .toBuilder()
                        .backwardSegment(directionalSegmentBackward.withAccessible(true))
                        .build(),
                roadSectionFragment
                        .toBuilder()
                        .backwardSegment(directionalSegmentBackward.withAccessible(true))
                        .build()
        ));
        boolean result = roadSection.isBackwardAccessible();
        assertThat(result).isTrue();
    }

    @Test
    void getForwardGeometries() {
        roadSection = roadSection.withRoadSectionFragments(List.of(
                        roadSectionFragment.withForwardSegment(directionalSegmentForward.withLineString(lineString1)),
                        roadSectionFragment.withForwardSegment(directionalSegmentForward.withLineString(lineString2))
                )
        );

        List<LineString> result = roadSection.getForwardGeometries();
        List<LineString> expected = List.of(lineString1, lineString2);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getForwardGeometries_noForwardSegment() {
        roadSection = roadSection.withRoadSectionFragments(List.of(
                        roadSectionFragment
                                .withForwardSegment(null)
                                .withBackwardSegment(directionalSegmentBackward)
                )
        );

        assertThat(catchThrowable(() -> roadSection.getForwardGeometries()))
                .withFailMessage("No forward geometry found for road section %s".formatted(roadSection.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getBackwardGeometries() {
        roadSection = roadSection.withRoadSectionFragments(List.of(
                        roadSectionFragment.withBackwardSegment(directionalSegmentForward.withLineString(lineString1)),
                        roadSectionFragment.withBackwardSegment(directionalSegmentForward.withLineString(lineString2))
                )
        );

        List<LineString> result = roadSection.getBackwardGeometries();
        List<LineString> expected = List.of(lineString1, lineString2);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getBackwardGeometries_noBackwardSegment() {
        roadSection = roadSection.withRoadSectionFragments(List.of(
                        roadSectionFragment
                                .withForwardSegment(directionalSegmentForward)
                                .withBackwardSegment(null)
                )
        );

        assertThat(catchThrowable(() -> roadSection.getBackwardGeometries()))
                .withFailMessage("No backward geometry found for road section %s".formatted(roadSection.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void validate() {

        validate(roadSection, List.of(), List.of());
    }

    @Test
    void validate_id_null() {

        roadSection = roadSection.withId(null);
        validate(roadSection, List.of("id"), List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_empty() {

        roadSection = roadSection.withRoadSectionFragments(List.of());
        validate(roadSection, List.of("roadSectionFragments"), List.of("must not be empty"));
    }

    @Test
    void validate_roadSectionFragments_id_null() {

        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment.withId(null)));
        validate(roadSection, List.of("roadSectionFragments[0].id"), List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_roadSection_null() {

        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment.withRoadSection(null)));
        validate(roadSection, List.of("roadSectionFragments[0].roadSection"), List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_id_null() {

        directionalSegmentForward = directionalSegmentForward.withId(null);
        directionalSegmentBackward = directionalSegmentBackward.withId(null);

        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));
        validate(roadSection, List.of("roadSectionFragments[0].forwardSegment.id"), List.of("must not be null"));

        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withBackwardSegment(directionalSegmentBackward)));
        validate(roadSection, List.of("roadSectionFragments[0].backwardSegment.id"), List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_direction_null() {

        directionalSegmentForward = directionalSegmentForward.withDirection(null);
        directionalSegmentBackward = directionalSegmentBackward.withDirection(null);

        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));
        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.direction"),
                List.of("must not be null"));

        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withBackwardSegment(directionalSegmentBackward)));
        validate(
                roadSection,
                List.of("roadSectionFragments[0].backwardSegment.direction"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_lineString_null() {

        directionalSegmentForward = directionalSegmentForward.withLineString(null);
        directionalSegmentBackward = directionalSegmentBackward.withLineString(null);

        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));
        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.lineString"),
                List.of("must not be null"));

        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withBackwardSegment(directionalSegmentBackward)));
        validate(
                roadSection,
                List.of("roadSectionFragments[0].backwardSegment.lineString"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_roadSectionFragment_null() {

        directionalSegmentForward = directionalSegmentForward.withRoadSectionFragment(null);
        directionalSegmentBackward = directionalSegmentBackward.withRoadSectionFragment(null);

        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));
        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.roadSectionFragment"),
                List.of("must not be null"));

        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withBackwardSegment(directionalSegmentBackward)));
        validate(
                roadSection,
                List.of("roadSectionFragments[0].backwardSegment.roadSectionFragment"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_id() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSigns(
                List.of(directionalSegmentForward.getTrafficSigns().getFirst()
                        .withId(null))
        );
        roadSectionFragment = roadSectionFragment.withForwardSegment(directionalSegmentForward);
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSigns[0].id"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_externalId() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSigns(
                List.of(directionalSegmentForward.getTrafficSigns().getFirst()
                        .withExternalId(null))
        );
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSigns[0].externalId"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_roadSectionId() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSigns(List.of(
                directionalSegmentForward.getTrafficSigns().getFirst().withRoadSectionId(null))
        );
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSigns[0].roadSectionId"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_trafficSignType() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSigns(List.of(
                directionalSegmentForward.getTrafficSigns().getFirst().withTrafficSignType(null))
        );
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSigns[0].trafficSignType"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_latitude() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSigns(List.of(
                directionalSegmentForward.getTrafficSigns().getFirst().withLatitude(null))
        );
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSigns[0].latitude"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_longitude() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSigns(List.of(
                directionalSegmentForward.getTrafficSigns().getFirst().withLongitude(null))
        );
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSigns[0].longitude"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_direction() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSigns(List.of(
                directionalSegmentForward.getTrafficSigns().getFirst().withDirection(null))
        );
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSigns[0].direction"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_fraction() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSigns(List.of(
                directionalSegmentForward.getTrafficSigns().getFirst().withFraction(null))
        );
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSigns[0].fraction"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_textSigns() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSigns(List.of(
                directionalSegmentForward.getTrafficSigns().getFirst().withTextSigns(null))
        );
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSigns[0].textSigns"),
                List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {

        return RoadSection.class;
    }
}
