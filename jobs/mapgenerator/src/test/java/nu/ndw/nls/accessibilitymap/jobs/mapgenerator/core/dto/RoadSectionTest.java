package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto;

import static org.mockito.Mockito.mock;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.unit.ValidationTest;
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
                .trafficSign(TrafficSign.builder()
                        .id(4)
                        .roadSectionId(1)
                        .externalId("externalId")
                        .direction(Direction.FORWARD)
                        .fraction(2d)
                        .longitude(3d)
                        .latitude(4d)
                        .textSigns(List.of())
                        .trafficSignType(TrafficSignType.C7)
                        .build())
                .build();

        directionalSegmentBackward = directionalSegmentForward.withDirection(Direction.BACKWARD);

        roadSectionFragment.setForwardSegment(directionalSegmentForward);
        roadSectionFragment.setBackwardSegment(directionalSegmentBackward);
    }

    @Test
    void validate_ok() {

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
    void validate_roadSectionFragments_forwardSegment_null() {

        roadSection = roadSection.withRoadSectionFragments(List.of(roadSectionFragment.withForwardSegment(null)));
        validate(roadSection, List.of("roadSectionFragments[0].forwardSegment"), List.of("must not be null"));
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

        directionalSegmentForward = directionalSegmentForward.withTrafficSign(
                directionalSegmentForward.getTrafficSign().withId(null)
        );
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSign.id"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_externalId() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSign(
                directionalSegmentForward.getTrafficSign().withExternalId(null)
        );
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSign.externalId"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_roadSectionId() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSign(
                directionalSegmentForward.getTrafficSign().withRoadSectionId(null)
        );
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSign.roadSectionId"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_trafficSignType() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSign(
                directionalSegmentForward.getTrafficSign().withTrafficSignType(null)
        );
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSign.trafficSignType"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_latitude() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSign(
                directionalSegmentForward.getTrafficSign().withLatitude(null)
        );
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSign.latitude"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_longitude() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSign(
                directionalSegmentForward.getTrafficSign().withLongitude(null)
        );
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSign.longitude"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_direction() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSign(
                directionalSegmentForward.getTrafficSign().withDirection(null)
        );
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSign.direction"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_fraction() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSign(
                directionalSegmentForward.getTrafficSign().withFraction(null)
        );
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSign.fraction"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionFragments_segment_trafficSign_textSigns() {

        directionalSegmentForward = directionalSegmentForward.withTrafficSign(
                directionalSegmentForward.getTrafficSign().withTextSigns(null)
        );
        roadSectionFragment.withForwardSegment(directionalSegmentForward);
        roadSection = roadSection.withRoadSectionFragments(List.of(
                roadSectionFragment.withForwardSegment(directionalSegmentForward)));

        validate(
                roadSection,
                List.of("roadSectionFragments[0].forwardSegment.trafficSign.textSigns"),
                List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {

        return RoadSection.class;
    }
}
