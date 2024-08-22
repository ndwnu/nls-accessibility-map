package nu.ndw.nls.accessibilitymap.accessibility.services;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.EdgeIteratorState;
import java.util.HashMap;
import nu.ndw.nls.accessibilitymap.accessibility.model.WindowTimeEncodedValue;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NetworkServiceTest {

    private static final long ROAD_SECTION_ID = 123L;
    private static final int EDGE_ID = 11;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @InjectMocks
    private NetworkService networkService;

    @Mock
    private HashMap<Long, Integer> edgeMap;

    @Mock
    private BaseGraph baseGraph;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private BooleanEncodedValue booleanEncodedValue;

    @BeforeEach
    void setUp() {
        when(networkGraphHopper.getEdgeMap()).thenReturn(edgeMap);
        when(edgeMap.get(ROAD_SECTION_ID)).thenReturn(EDGE_ID);
        when(networkGraphHopper.getBaseGraph()).thenReturn(baseGraph);
        when(baseGraph.getEdgeIteratorStateForKey(EDGE_ID)).thenReturn(edgeIteratorState);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(encodingManager.getBooleanEncodedValue("hgv_and_bus_access_forbidden_windowed"))
                .thenReturn(booleanEncodedValue);
    }

    @Test
    void hasWindowTimeByRoadSectionId_ok_forwardTrue() {
        when(edgeIteratorState.get(booleanEncodedValue)).thenReturn(true);
        assertThat(networkService.hasWindowTimeByRoadSectionId(ROAD_SECTION_ID, WindowTimeEncodedValue.C7B)).isTrue();
    }

    @Test
    void hasWindowTimeByRoadSectionId_ok_backwardTrue() {
        when(edgeIteratorState.get(booleanEncodedValue)).thenReturn(false);
        when(edgeIteratorState.getReverse(booleanEncodedValue)).thenReturn(true);
        assertThat(networkService.hasWindowTimeByRoadSectionId(ROAD_SECTION_ID, WindowTimeEncodedValue.C7B)).isTrue();
    }

    @Test
    void hasWindowTimeByRoadSectionId_ok_backwardFalse() {
        when(edgeIteratorState.get(booleanEncodedValue)).thenReturn(false);
        when(edgeIteratorState.getReverse(booleanEncodedValue)).thenReturn(false);
        assertThat(networkService.hasWindowTimeByRoadSectionId(ROAD_SECTION_ID, WindowTimeEncodedValue.C7B)).isFalse();
    }
}