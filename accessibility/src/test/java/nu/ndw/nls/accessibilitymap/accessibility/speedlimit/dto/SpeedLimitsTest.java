package nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
}
