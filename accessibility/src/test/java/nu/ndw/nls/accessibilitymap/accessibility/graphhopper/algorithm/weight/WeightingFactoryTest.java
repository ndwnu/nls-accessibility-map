package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.weight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.PMap;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto.SpeedLimits;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeightingFactoryTest {

    private WeightingFactory weightingFactory;

    @Mock
    private EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private NetworkData networkData;

    @Mock
    private SpeedLimits speedLimits;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private Weighting baseWeighting;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private Weighting wrappedWeighting;

    @Captor
    private ArgumentCaptor<VariableSpeedLimitWeighting> variableSpeedLimitWeightingCaptor;

    @BeforeEach
    void setUp() {
        weightingFactory = new WeightingFactory(edgeIteratorStateReverseExtractor);
    }

    @Test
    void createWeighting() {
        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(networkGraphHopper.createWeighting(eq(NetworkConstants.CAR_PROFILE), any(PMap.class))).thenReturn(baseWeighting);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(queryGraph.wrapWeighting(variableSpeedLimitWeightingCaptor.capture())).thenReturn(wrappedWeighting);

        assertThat(weightingFactory.createWeighting(queryGraph, networkData, speedLimits)).isEqualTo(wrappedWeighting);

        VariableSpeedLimitWeighting captured = variableSpeedLimitWeightingCaptor.getValue();
        assertThat(getField(captured, "sourceWeighting")).isEqualTo(baseWeighting);
        assertThat(getField(captured, "speedLimits")).isEqualTo(speedLimits);
        assertThat(getField(captured, "encodingManager")).isEqualTo(encodingManager);
        assertThat(getField(captured, "edgeIteratorStateReverseExtractor")).isEqualTo(edgeIteratorStateReverseExtractor);
    }
}
