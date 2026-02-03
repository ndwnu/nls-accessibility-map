package nu.ndw.nls.accessibilitymap.accessibility.reason.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.reason.graphhopper.PathsToReasonsMapper;
import nu.ndw.nls.accessibilitymap.accessibility.reason.mapper.AccessibilityReasonsMapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityContext;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityReasonServiceTest {

    private AccessibilityReasonService accessibilityReasonService;

    @Mock
    private AccessibilityReasonsMapper accessibilityReasonsMapper;

    @Mock
    private RoutingAlgorithmFactory routingAlgorithmFactory;

    @Mock
    private PathsToReasonsMapper pathsToReasonsMapper;

    @Mock
    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private AccessibilityNetwork accessibilityNetwork;

    @Mock
    private AccessibilityContext accessibilityContext;

    @Mock
    private BaseGraph baseGraph;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private Weighting weighting;

    @Mock
    private LocationIndexTree locationIndexTree;

    @Mock
    private Snap startSnap;

    @Mock
    private Snap endSnap;

    @Mock
    private Restrictions restrictions;

    @Mock
    private AccessibilityReasons accessibilityReasons;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private RoutingAlgorithm routeRoutingAlgorithm;

    private List<Path> routes;

    @Mock
    private Path path;

    private MockedStatic<QueryGraph> queryGraphStaticMock;

    @Mock
    private List<List<AccessibilityReason>> accessibilityReasonsList;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        routes = List.of(path);

        accessibilityReasonService = new AccessibilityReasonService(
                accessibilityReasonsMapper,
                routingAlgorithmFactory,
                pathsToReasonsMapper);

        queryGraphStaticMock = Mockito.mockStatic(QueryGraph.class);
    }

    @AfterEach
    void tearDown() {

        queryGraphStaticMock.close();
    }

    @Test
    void calculateReasons() {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startLocationLatitude(1d)
                .startLocationLongitude(2d)
                .endLocationLatitude(3d)
                .endLocationLongitude(4d)
                .build();

        when(accessibilityNetwork.getAccessibilityContext()).thenReturn(accessibilityContext);
        when(accessibilityContext.graphHopperNetwork()).thenReturn(graphHopperNetwork);
        when(graphHopperNetwork.network()).thenReturn(networkGraphHopper);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(networkGraphHopper.getBaseGraph()).thenReturn(baseGraph);
        when(networkGraphHopper.createWeighting(eq(NetworkConstants.CAR_PROFILE), argThat(PMap::isEmpty))).thenReturn(weighting);
        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(locationIndexTree.findClosest(
                accessibilityRequest.startLocationLatitude(),
                accessibilityRequest.startLocationLongitude(),
                EdgeFilter.ALL_EDGES)).thenReturn(startSnap);

        when(locationIndexTree.findClosest(
                accessibilityRequest.endLocationLatitude(),
                accessibilityRequest.endLocationLongitude(),
                EdgeFilter.ALL_EDGES)).thenReturn(endSnap);

        queryGraphStaticMock.when(() -> QueryGraph.create(baseGraph, startSnap, endSnap)).thenReturn(queryGraph);

        when(routingAlgorithmFactory.createAlgo(
                eq(queryGraph), eq(weighting), argThat(algorithmOptions ->
                        algorithmOptions.getHints().getBool("pass_through", false)
                        && algorithmOptions.getAlgorithm().equals("dijkstrabi")
                        && algorithmOptions.getTraversalMode() == TraversalMode.NODE_BASED)))
                .thenReturn(routeRoutingAlgorithm);

        when(startSnap.getClosestNode()).thenReturn(1);
        when(endSnap.getClosestNode()).thenReturn(2);
        when(routeRoutingAlgorithm.calcPaths(1, 2)).thenReturn(routes);
        when(path.isFound()).thenReturn(true);

        when(accessibilityNetwork.getRestrictions()).thenReturn(restrictions);
        when(accessibilityReasonsMapper.mapRestrictions(restrictions)).thenReturn(accessibilityReasons);
        when(pathsToReasonsMapper.mapRoutesToReasons(routes, accessibilityReasons, encodingManager)).thenReturn(accessibilityReasonsList);

        List<List<AccessibilityReason>> result = accessibilityReasonService.calculateReasons(accessibilityRequest, accessibilityNetwork);

        assertThat(result).isEqualTo(accessibilityReasonsList);
    }

    @Test
    void calculateReasons_noRoutesFound() {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startLocationLatitude(1d)
                .startLocationLongitude(2d)
                .endLocationLatitude(3d)
                .endLocationLongitude(4d)
                .build();

        when(accessibilityNetwork.getAccessibilityContext()).thenReturn(accessibilityContext);
        when(accessibilityContext.graphHopperNetwork()).thenReturn(graphHopperNetwork);
        when(graphHopperNetwork.network()).thenReturn(networkGraphHopper);
        when(networkGraphHopper.getBaseGraph()).thenReturn(baseGraph);
        when(networkGraphHopper.createWeighting(eq(NetworkConstants.CAR_PROFILE), argThat(PMap::isEmpty))).thenReturn(weighting);
        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(locationIndexTree.findClosest(
                accessibilityRequest.startLocationLatitude(),
                accessibilityRequest.startLocationLongitude(),
                EdgeFilter.ALL_EDGES)).thenReturn(startSnap);

        when(locationIndexTree.findClosest(
                accessibilityRequest.endLocationLatitude(),
                accessibilityRequest.endLocationLongitude(),
                EdgeFilter.ALL_EDGES)).thenReturn(endSnap);

        queryGraphStaticMock.when(() -> QueryGraph.create(baseGraph, startSnap, endSnap)).thenReturn(queryGraph);

        when(routingAlgorithmFactory.createAlgo(
                eq(queryGraph), eq(weighting), argThat(algorithmOptions ->
                        algorithmOptions.getHints().getBool("pass_through", false)
                        && algorithmOptions.getAlgorithm().equals("dijkstrabi")
                        && algorithmOptions.getTraversalMode() == TraversalMode.NODE_BASED)))
                .thenReturn(routeRoutingAlgorithm);

        when(startSnap.getClosestNode()).thenReturn(1);
        when(endSnap.getClosestNode()).thenReturn(2);
        when(routeRoutingAlgorithm.calcPaths(1, 2)).thenReturn(routes);
        when(path.isFound()).thenReturn(false);

        List<List<AccessibilityReason>> result = accessibilityReasonService.calculateReasons(accessibilityRequest, accessibilityNetwork);

        assertThat(result).isEmpty();
        loggerExtension.containsLog(Level.WARN, "No routes found for request: %s".formatted(accessibilityRequest));
    }
}
