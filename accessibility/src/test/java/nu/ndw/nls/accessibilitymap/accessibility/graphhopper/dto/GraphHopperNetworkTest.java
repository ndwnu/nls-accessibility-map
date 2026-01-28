package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.index.Snap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphHopperNetworkTest {

    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private Restrictions restrictions;

    @Mock
    private Restriction restriction;

    @Mock
    private Snap snap;

    @Mock
    private Snap fromSnap;

    @Mock
    private Snap destinationSnap;

    @BeforeEach
    void setUp() {

        graphHopperNetwork = new GraphHopperNetwork(
                networkGraphHopper,
                1,
                queryGraph,
                restrictions,
                Map.of(2, List.of(restriction)),
                fromSnap,
                destinationSnap
        );
    }

    @Test
    void validate_getters() {
        assertThat(graphHopperNetwork.getQueryGraph()).isEqualTo(queryGraph);
        assertThat(graphHopperNetwork.getNetwork()).isEqualTo(networkGraphHopper);
        assertThat(graphHopperNetwork.getNwbVersion()).isEqualTo(1);
        assertThat(graphHopperNetwork.getFrom()).isEqualTo(fromSnap);
        assertThat(graphHopperNetwork.getDestination()).isEqualTo(destinationSnap);
        assertThat(graphHopperNetwork.getRestrictions()).isEqualTo(restrictions);
        assertThat(graphHopperNetwork.getBlockedEdges()).isEqualTo(Set.of(2));
        assertThat(graphHopperNetwork.getRestrictionsByEdgeKey()).isEqualTo(Map.of(2, List.of(restriction)));
    }

    @Test
    void toStringTest() {
        assertThat(graphHopperNetwork)
                .hasToString("GraphHopperNetwork[network=networkGraphHopper, queryGraph=queryGraph, "
                             + "restrictions=restrictions, restrictionsByEdgeKey={2=[restriction]}, blockedEdges=[2]]");
    }
}
