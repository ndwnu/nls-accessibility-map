package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.weight;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Objects;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.speedlimit.SpeedLimit;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto.SpeedLimits;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VariableSpeedLimitWeightingTest {

    private VariableSpeedLimitWeighting variableSpeedLimitWeighting;

    @Mock
    private Weighting sourceWeighting;

    @Mock
    private SpeedLimits speedLimits;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private IntEncodedValue intEncodedValue;

    @BeforeEach
    void setUp() {

        variableSpeedLimitWeighting = new VariableSpeedLimitWeighting(
                sourceWeighting,
                speedLimits,
                encodingManager,
                edgeIteratorStateReverseExtractor);
    }

    @Test
    void calcEdgeWeight() {
        when(sourceWeighting.calcEdgeWeight(edgeIteratorState, true)).thenReturn(2.0);

        assertThat(variableSpeedLimitWeighting.calcEdgeWeight(edgeIteratorState, true)).isEqualTo(2.0);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            23500,  33,     FORWARD,    1,  2563636
            23500,  33,     BACKWARD,   1,  2563636
            23500,  null,   FORWARD,    1,  1
            """)
    void calcEdgeMillis(
            double distanceInMeters,
            Double speedInKmPerHour,
            Direction direction,
            long sourceWeighingCalcEdgeMillis,
            long expectedTravelTime) {
        int roadSectionId = 2;

        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIteratorState)).thenReturn(direction.isBackward());
        when(edgeIteratorState.get(intEncodedValue)).thenReturn(roadSectionId);
        if (Objects.isNull(speedInKmPerHour)) {
            when(speedLimits.findByRoadSectionId(roadSectionId, direction)).thenReturn(Optional.empty());
            when(sourceWeighting.calcEdgeMillis(edgeIteratorState, true)).thenReturn(sourceWeighingCalcEdgeMillis);
            assertThat(variableSpeedLimitWeighting.calcEdgeMillis(edgeIteratorState, true)).isEqualTo(expectedTravelTime);
        } else {
            when(edgeIteratorState.getDistance()).thenReturn(distanceInMeters);

            SpeedLimit speedLimit = new SpeedLimit(roadSectionId, direction, speedInKmPerHour);
            when(speedLimits.findByRoadSectionId(roadSectionId, direction)).thenReturn(Optional.of(speedLimit));
            assertThat(variableSpeedLimitWeighting.calcEdgeMillis(edgeIteratorState, true)).isEqualTo(expectedTravelTime);
        }
    }

    @Test
    void calcTurnWeight() {
        when(sourceWeighting.calcTurnWeight(1, 2, 3)).thenReturn(4.0);

        assertThat(variableSpeedLimitWeighting.calcTurnWeight(1, 2, 3)).isEqualTo(4.0);
    }

    @Test
    void calcTurnMillis() {
        when(sourceWeighting.calcTurnMillis(1, 2, 3)).thenReturn(4L);

        assertThat(variableSpeedLimitWeighting.calcTurnMillis(1, 2, 3)).isEqualTo(4L);
    }

    @Test
    void hasTurnCosts() {
        when(sourceWeighting.hasTurnCosts()).thenReturn(true);

        assertThat(variableSpeedLimitWeighting.hasTurnCosts()).isTrue();
    }

    @Test
    void getName() {
        when(sourceWeighting.getName()).thenReturn("name");

        assertThat(variableSpeedLimitWeighting.getName()).isEqualTo("name");
    }
}
