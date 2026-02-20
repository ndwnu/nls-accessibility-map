package nu.ndw.nls.accessibilitymap.accessibility.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityNetworkTest {

    @Mock
    private NetworkData networkData;

    @Mock
    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private Weighting weighting;

    @Mock
    private Snap from;

    @Mock
    private Snap destination;

    @Mock
    private Restriction restriction;

    @Test
    void toStringTest() {

        Restrictions restrictions = new Restrictions();
        restrictions.add(restriction);

        Map<Integer, List<Restriction>> restrictionsByEdgeKey = Map.of(1, List.of(restriction));

        when(networkData.getGraphHopperNetwork()).thenReturn(graphHopperNetwork);
        when(graphHopperNetwork.network()).thenReturn(networkGraphHopper);
        when(networkGraphHopper.createWeighting(
                eq(NetworkConstants.CAR_PROFILE),
                assertArg(pMap -> assertThat(pMap).hasToString("{}")))
        ).thenReturn(weighting);

        AccessibilityNetwork accessibilityNetwork = new AccessibilityNetwork(
                networkData,
                queryGraph,
                restrictions,
                restrictionsByEdgeKey,
                from,
                destination);

        assertThat(accessibilityNetwork)
                .hasToString(
                        "GraphHopperNetwork(networkData=networkData, restrictions=[restriction], restrictionsByEdgeKey={1=[restriction]}, blockedEdges=[1], from=from, destination=destination)");
    }
}
