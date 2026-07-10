package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignsTest {

    @Mock
    private TrafficSign trafficSign1;

    @Mock
    private TrafficSign trafficSign2;

    @Test
    void constructor() {

        assertThat(new TrafficSigns())
                .isNotNull()
                .isEmpty();

        assertThat(new TrafficSigns(trafficSign1, trafficSign2))
                .isNotNull()
                .containsExactlyInAnyOrder(trafficSign1, trafficSign2);

        assertThat(new TrafficSigns(List.of(trafficSign1, trafficSign2)))
                .isNotNull()
                .containsExactlyInAnyOrder(trafficSign1, trafficSign2);
    }
}
