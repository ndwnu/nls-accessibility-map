package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.roadsection;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionRestrictionTest extends ValidationTest {

    private RoadSectionRestriction roadSectionRestriction;

    @BeforeEach
    void setUp() {

        roadSectionRestriction = RoadSectionRestriction.builder()
                .id(1)
                .direction(Direction.BACKWARD)
                .startFraction(0.2)
                .endFraction(0.3)
                .networkSnappedLatitude(2D)
                .networkSnappedLongitude(3D)
                .build();
    }

    @Test
    void isRestrictive() {

        assertThat(roadSectionRestriction.isRestrictive(null)).isTrue();
    }

    @Test
    void roadSectionId() {

        assertThat(roadSectionRestriction.roadSectionId()).isEqualTo(1);
    }

    @Test
    void toStringTest() {

        assertThat(roadSectionRestriction).hasToString(
                "RoadSectionRestriction{id=1, direction=BACKWARD, startFraction=0.2, endFraction=0.3, networkSnappedLatitude=2.0, networkSnappedLongitude=3.0}");
    }

    @Test
    void validate_id_null() {
        roadSectionRestriction = roadSectionRestriction.withId(null);
        validate(
                roadSectionRestriction,
                List.of("id"),
                List.of("must not be null"));
    }

    @Test
    void validate_direction_null() {
        roadSectionRestriction = roadSectionRestriction.withDirection(null);
        validate(
                roadSectionRestriction,
                List.of("direction"),
                List.of("must not be null"));
    }

    @Test
    void validate_startFraction_null() {
        roadSectionRestriction = roadSectionRestriction.withStartFraction(null);
        validate(
                roadSectionRestriction,
                List.of("startFraction"),
                List.of("must not be null"));
    }

    @Test
    void validate_endFraction_null() {
        roadSectionRestriction = roadSectionRestriction.withEndFraction(null);
        validate(
                roadSectionRestriction,
                List.of("endFraction"),
                List.of("must not be null"));
    }

    @Test
    void validate_networkSnappedLatitude_null() {
        roadSectionRestriction = roadSectionRestriction.withNetworkSnappedLatitude(null);
        validate(
                roadSectionRestriction,
                List.of("networkSnappedLatitude"),
                List.of("must not be null"));
    }

    @Test
    void validate_networkSnappedLongitude_null() {
        roadSectionRestriction = roadSectionRestriction.withNetworkSnappedLongitude(null);
        validate(
                roadSectionRestriction,
                List.of("networkSnappedLongitude"),
                List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {
        return roadSectionRestriction.getClass();
    }
}
