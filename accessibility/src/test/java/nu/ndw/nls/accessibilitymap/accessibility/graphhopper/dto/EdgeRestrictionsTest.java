package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EdgeRestrictionsTest {

    @Mock
    private TrafficSign trafficSign1;

    @Mock
    private TrafficSign trafficSign2;

    @Mock
    private TrafficSign trafficSign3;

    @Test
    void EdgeRestrictions() {

        EdgeRestrictions edgeRestrictions = new EdgeRestrictions(
                List.of(EdgeRestriction.builder()
                                .edgeKey(2)
                                .trafficSign(trafficSign1)
                                .build(),
                        EdgeRestriction.builder()
                                .edgeKey(2)
                                .trafficSign(trafficSign2)
                                .build(),
                        EdgeRestriction.builder()
                                .edgeKey(3)
                                .trafficSign(trafficSign3)
                                .build()));

        assertThat(edgeRestrictions.getBlockedEdges()).containsExactly(2, 3);

        assertThat(edgeRestrictions.getTrafficSignsByEdgeKey()).hasSize(2);
        assertThat(edgeRestrictions.getTrafficSignsByEdgeKey()).contains(entry(2, List.of(trafficSign1, trafficSign2)));
        assertThat(edgeRestrictions.getTrafficSignsByEdgeKey()).contains(entry(3, List.of(trafficSign3)));
    }
}
