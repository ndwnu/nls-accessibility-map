package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DirectionalSegmentTest {

    private DirectionalSegment directionalSegment;

    @Mock
    private TrafficSign trafficSign;

    @Test
    void hasTrafficSigns() {

        directionalSegment = DirectionalSegment.builder()
                .trafficSigns(List.of(trafficSign))
                .build();

        assertThat(directionalSegment.hasTrafficSigns()).isTrue();
    }

    @Test
    void hasTrafficSign_noTrafficSigns() {

        directionalSegment = DirectionalSegment.builder()
                .build();

        assertThat(directionalSegment.hasTrafficSigns()).isFalse();
    }
}
