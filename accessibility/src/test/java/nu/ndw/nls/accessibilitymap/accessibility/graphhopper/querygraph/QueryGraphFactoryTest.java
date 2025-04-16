package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.index.Snap;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.TrafficSignSnap;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryGraphFactoryTest {

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private BaseGraph baseGraph;

    @Mock
    private Snap startSegment;

    @Mock
    private Snap snapForTrafficSign;

    @Mock
    private TrafficSignSnap trafficSignSnap;

    @Mock
    private QueryGraph queryGraph;

    private static MockedStatic<QueryGraph> queryGraphStaticMock;

    @InjectMocks
    private QueryGraphFactory queryGraphFactory;

    @BeforeAll
    static void setUp() {
        queryGraphStaticMock = Mockito.mockStatic(QueryGraph.class);
    }

    @AfterAll
    static void tearDown() {
        queryGraphStaticMock.close();
    }

    @Test
    void createQueryGraph() {
        when(networkGraphHopper.getBaseGraph()).thenReturn(baseGraph);
        when(trafficSignSnap.getSnap()).thenReturn(snapForTrafficSign);
        queryGraphStaticMock.when(() -> QueryGraph.create(baseGraph, List.of(snapForTrafficSign, startSegment))).thenReturn(queryGraph);

        QueryGraph result = queryGraphFactory.createQueryGraph(List.of(trafficSignSnap), startSegment);

        assertThat(result).isEqualTo(queryGraph);
    }
}
