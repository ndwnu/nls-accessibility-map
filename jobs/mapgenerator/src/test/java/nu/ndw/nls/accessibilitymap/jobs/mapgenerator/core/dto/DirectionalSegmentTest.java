package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
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
    void hasTrafficSign_ok() {

        directionalSegment = DirectionalSegment.builder()
                .trafficSign(trafficSign)
                .build();

        assertThat(directionalSegment.hasTrafficSign()).isTrue();
    }

    @Test
    void hasTrafficSign_ok_noTrafficSign() {

        directionalSegment = DirectionalSegment.builder()
                .build();

        assertThat(directionalSegment.hasTrafficSign()).isFalse();
    }
}
