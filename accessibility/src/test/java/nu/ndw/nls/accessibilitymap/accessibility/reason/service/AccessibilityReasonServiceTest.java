package nu.ndw.nls.accessibilitymap.accessibility.reason.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasonGroup;
import nu.ndw.nls.accessibilitymap.accessibility.reason.graphhopper.PathsToReasonsMapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityReasonServiceTest {

    private AccessibilityReasonService accessibilityReasonService;

    @Mock
    private RoutingAlgorithmFactory routingAlgorithmFactory;

    @Mock
    private PathsToReasonsMapper pathsToReasonsMapper;

    @Mock
    private AccessibilityNetwork accessibilityNetwork;

    @Mock
    private Weighting weighting;

    @Mock
    private RoutingAlgorithm routeRoutingAlgorithm;

    @Mock
    private Path path;

    @Mock
    private List<AccessibilityReasonGroup> accessibilityReasonGroups;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @Mock
    private Snap from;

    @Mock
    private Snap destination;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private Map<Integer, DirectionalSegment> directionalSegmentsById;

    @Mock
    private DirectionalSegment directionalSegment;

    @Mock
    private RoadSectionFragment roadSectionFragment;

    @BeforeEach
    void setUp() {

        accessibilityReasonService = new AccessibilityReasonService(
                routingAlgorithmFactory,
                pathsToReasonsMapper);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,
            false
            """)
    void calculateReasons(boolean effectivelyAccessible) {

        if (effectivelyAccessible) {
            when(directionalSegment.getRoadSectionFragment()).thenReturn(roadSectionFragment);
            when(roadSectionFragment.isAccessibleFromAnySegment()).thenReturn(false);
        } else {
            when(directionalSegment.isAccessible()).thenReturn(false);
        }

        when(accessibilityNetwork.getFrom()).thenReturn(from);
        when(accessibilityNetwork.getDestination()).thenReturn(destination);
        when(accessibilityNetwork.getQueryGraph()).thenReturn(queryGraph);
        when(accessibilityNetwork.getWeighting()).thenReturn(weighting);

        when(routingAlgorithmFactory.createAlgo(
                eq(queryGraph), eq(weighting), argThat(algorithmOptions ->
                        algorithmOptions.getHints().getBool("pass_through", false)
                                && algorithmOptions.getAlgorithm().equals("dijkstrabi")
                                && algorithmOptions.getTraversalMode() == TraversalMode.NODE_BASED)))
                .thenReturn(routeRoutingAlgorithm);

        when(from.getClosestNode()).thenReturn(1);
        when(destination.getClosestNode()).thenReturn(2);
        when(routeRoutingAlgorithm.calcPaths(1, 2)).thenReturn(List.of(path));
        when(path.isFound()).thenReturn(true);

        when(pathsToReasonsMapper.mapRoutesToReasons(List.of(path), directionalSegmentsById)).thenReturn(
                accessibilityReasonGroups);

        List<AccessibilityReasonGroup> actualAccessibilityReasonGroups = accessibilityReasonService.calculateReasons(
                Optional.of(directionalSegment),
                directionalSegmentsById,
                accessibilityNetwork, effectivelyAccessible);

        assertThat(actualAccessibilityReasonGroups).isEqualTo(accessibilityReasonGroups);
    }

    @Test
    void calculateReasons_toRoadSection_isNotRestrictedInAnyDirection() {

        when(directionalSegment.isAccessible()).thenReturn(true);

        List<AccessibilityReasonGroup> actualAccessibilityReasonGroups = accessibilityReasonService.calculateReasons(
                Optional.of(directionalSegment),
                directionalSegmentsById,
                accessibilityNetwork, false);

        assertThat(actualAccessibilityReasonGroups).isEmpty();
    }

    @Test
    void calculateReasons_noRoutes() {

        when(directionalSegment.isAccessible()).thenReturn(false);

        when(accessibilityNetwork.getFrom()).thenReturn(from);
        when(accessibilityNetwork.getDestination()).thenReturn(destination);
        when(accessibilityNetwork.getQueryGraph()).thenReturn(queryGraph);
        when(accessibilityNetwork.getWeighting()).thenReturn(weighting);

        when(routingAlgorithmFactory.createAlgo(
                eq(queryGraph), eq(weighting), argThat(algorithmOptions ->
                        algorithmOptions.getHints().getBool("pass_through", false)
                                && algorithmOptions.getAlgorithm().equals("dijkstrabi")
                                && algorithmOptions.getTraversalMode() == TraversalMode.NODE_BASED)))
                .thenReturn(routeRoutingAlgorithm);

        when(from.getClosestNode()).thenReturn(1);
        when(destination.getClosestNode()).thenReturn(2);
        when(routeRoutingAlgorithm.calcPaths(1, 2)).thenReturn(List.of());

        List<AccessibilityReasonGroup> actualAccessibilityReasonGroups = accessibilityReasonService.calculateReasons(
                Optional.of(directionalSegment),
                directionalSegmentsById,
                accessibilityNetwork, false);

        assertThat(actualAccessibilityReasonGroups).isEmpty();
    }

    @Test
    void calculateReasons_pathNotFound() {

        when(directionalSegment.isAccessible()).thenReturn(false);

        when(accessibilityNetwork.getFrom()).thenReturn(from);
        when(accessibilityNetwork.getDestination()).thenReturn(destination);
        when(accessibilityNetwork.getQueryGraph()).thenReturn(queryGraph);
        when(accessibilityNetwork.getWeighting()).thenReturn(weighting);

        when(routingAlgorithmFactory.createAlgo(
                eq(queryGraph), eq(weighting), argThat(algorithmOptions ->
                        algorithmOptions.getHints().getBool("pass_through", false)
                                && algorithmOptions.getAlgorithm().equals("dijkstrabi")
                                && algorithmOptions.getTraversalMode() == TraversalMode.NODE_BASED)))
                .thenReturn(routeRoutingAlgorithm);

        when(from.getClosestNode()).thenReturn(1);
        when(destination.getClosestNode()).thenReturn(2);
        when(routeRoutingAlgorithm.calcPaths(1, 2)).thenReturn(List.of(path));
        when(path.isFound()).thenReturn(false);

        List<AccessibilityReasonGroup> actualAccessibilityReasonGroups = accessibilityReasonService.calculateReasons(
                Optional.of(directionalSegment),
                directionalSegmentsById,
                accessibilityNetwork, false);

        assertThat(actualAccessibilityReasonGroups).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true
            false
            """)
    void calculateReasons_noDestination(boolean effectivelyAccessible) {


        if (effectivelyAccessible) {
            when(directionalSegment.getRoadSectionFragment()).thenReturn(roadSectionFragment);
            when(roadSectionFragment.isAccessibleFromAnySegment()).thenReturn(false);
        } else {
            when(directionalSegment.isAccessible()).thenReturn(false);
        }

        when(accessibilityNetwork.getFrom()).thenReturn(from);
        when(accessibilityNetwork.getDestination()).thenReturn(null);

        List<AccessibilityReasonGroup> actualAccessibilityReasonGroups = accessibilityReasonService.calculateReasons(
                Optional.of(directionalSegment),
                directionalSegmentsById,
                accessibilityNetwork, effectivelyAccessible);

        assertThat(actualAccessibilityReasonGroups).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true
            false
            """)
    void calculateReasons_noToDirectionalSegment(boolean effectivelyAccessible) {
        List<AccessibilityReasonGroup> actualAccessibilityReasonGroups = accessibilityReasonService.calculateReasons(
                Optional.empty(),
                directionalSegmentsById,
                accessibilityNetwork, effectivelyAccessible);
        assertThat(actualAccessibilityReasonGroups).isEmpty();
    }
}
