package nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.speedlimit.SpeedLimit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpeedLimitsTest {

    @Mock
    private SpeedLimit speedLimit1;

    @Mock
    private SpeedLimit speedLimit2;

    @Mock
    private SpeedLimit speedLimit3;

    @Test
    void constructor() {

        assertThat(new SpeedLimits())
                .isNotNull()
                .isEmpty();

        assertThat(new SpeedLimits(speedLimit1, speedLimit2))
                .isNotNull()
                .containsExactlyInAnyOrder(speedLimit1, speedLimit2);

        assertThat(new SpeedLimits(List.of(speedLimit1, speedLimit2)))
                .isNotNull()
                .containsExactlyInAnyOrder(speedLimit1, speedLimit2);
    }

    @Test
    void findByRoadSectionId() {
        when(speedLimit1.roadSectionId()).thenReturn(1);
        when(speedLimit1.direction()).thenReturn(Direction.FORWARD);
        when(speedLimit2.roadSectionId()).thenReturn(1);
        when(speedLimit2.direction()).thenReturn(Direction.BACKWARD);
        when(speedLimit3.roadSectionId()).thenReturn(2);
        when(speedLimit3.direction()).thenReturn(Direction.BACKWARD);

        SpeedLimits speedLimits = new SpeedLimits(speedLimit1, speedLimit2, speedLimit3);

        assertThat(speedLimits.findByRoadSectionId(1, Direction.FORWARD)).contains(speedLimit1);
        assertThat(speedLimits.findByRoadSectionId(1, Direction.BACKWARD)).contains(speedLimit2);
        assertThat(speedLimits.findByRoadSectionId(2, Direction.FORWARD)).isEmpty();
        assertThat(speedLimits.findByRoadSectionId(2, Direction.BACKWARD)).contains(speedLimit3);
        assertThat(speedLimits.findByRoadSectionId(3, Direction.BACKWARD)).isEmpty();
    }
}
