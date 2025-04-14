package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import static org.mockito.Mockito.mock;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
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

        directionalSegmentForward = mock(DirectionalSegment.class);
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
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
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
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
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
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
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
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
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
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
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
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
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
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
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
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
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
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
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
