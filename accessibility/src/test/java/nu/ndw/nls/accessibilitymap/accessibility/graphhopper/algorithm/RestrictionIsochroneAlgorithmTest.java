package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.exploration.ExploreLimit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestrictionIsochroneAlgorithmTest {

    private RestrictionIsochroneAlgorithm restrictionIsochroneAlgorithm;

    @Mock
    private Graph graph;

    @Mock
    private NodeAccess nodeAccess;

    @Mock
    private EdgeExplorer edgeExplorer;

    @Mock
    private EdgeIterator edgeIterator;

    @Mock
    private Weighting weighting;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private ExploreLimit<RestrictionsIsochroneLabel> exploreLimit;

    @Mock
    private Restriction restriction;

    private Map<Integer, List<Restriction>> restrictionsByEdgeKey;

    @BeforeEach
    void setUp() {
        when(graph.getNodeAccess()).thenReturn(nodeAccess);
        when(graph.createEdgeExplorer()).thenReturn(edgeExplorer);
        when(weighting.hasTurnCosts()).thenReturn(false);
        restrictionsByEdgeKey = new HashMap<>();
        restrictionIsochroneAlgorithm = new RestrictionIsochroneAlgorithm(
                graph,
                encodingManager,
                TraversalMode.NODE_BASED,
                false,
                weighting,
                exploreLimit,
                Comparator.comparingDouble(RestrictionsIsochroneLabel::getWeight),
                restrictionsByEdgeKey);
    }

    @Test
    void createNewIsoLabel_withMatchingEdgeKey_propagatesRestrictionsToLabel() {
        restrictionsByEdgeKey.put(5, List.of(restriction));

        EdgeIterator emptyIterator = mock(EdgeIterator.class);
        when(edgeExplorer.setBaseNode(1)).thenReturn(edgeIterator);
        when(edgeExplorer.setBaseNode(2)).thenReturn(emptyIterator);
        when(edgeIterator.next()).thenReturn(true, false);
        when(edgeIterator.getEdge()).thenReturn(10);
        when(edgeIterator.getEdgeKey()).thenReturn(5);
        when(edgeIterator.getAdjNode()).thenReturn(2);
        when(edgeIterator.getDistance()).thenReturn(100.0);
        when(weighting.calcEdgeWeight(edgeIterator, false)).thenReturn(10.0);
        when(weighting.calcEdgeMillis(edgeIterator, false)).thenReturn(3600L);
        when(emptyIterator.next()).thenReturn(false);
        when(exploreLimit.isInLimit(any(RestrictionsIsochroneLabel.class), eq(encodingManager))).thenReturn(true);

        List<RestrictionsIsochroneLabel> visited = new ArrayList<>();
        restrictionIsochroneAlgorithm.search(1, visited::add);

        assertThat(visited).hasSize(2);
        assertThat(visited.get(0).getRestrictions()).isEmpty();
        assertThat(visited.get(1).getRestrictions()).containsExactly(restriction);
    }

    @Test
    void createNewIsoLabel_withNoMatchingEdgeKey_createsLabelWithEmptyRestrictions() {
        EdgeIterator emptyIterator = mock(EdgeIterator.class);
        when(edgeExplorer.setBaseNode(1)).thenReturn(edgeIterator);
        when(edgeExplorer.setBaseNode(2)).thenReturn(emptyIterator);
        when(edgeIterator.next()).thenReturn(true, false);
        when(edgeIterator.getEdge()).thenReturn(10);
        when(edgeIterator.getAdjNode()).thenReturn(2);
        when(edgeIterator.getDistance()).thenReturn(100.0);
        when(weighting.calcEdgeWeight(edgeIterator, false)).thenReturn(10.0);
        when(weighting.calcEdgeMillis(edgeIterator, false)).thenReturn(3600L);
        when(emptyIterator.next()).thenReturn(false);
        when(exploreLimit.isInLimit(any(RestrictionsIsochroneLabel.class), eq(encodingManager))).thenReturn(true);

        List<RestrictionsIsochroneLabel> visited = new ArrayList<>();
        restrictionIsochroneAlgorithm.search(1, visited::add);

        assertThat(visited).hasSize(2);
        assertThat(visited.get(1).getRestrictions()).isEmpty();
    }

    @Test
    void mergeEqualWeightedIsoLabels_doesNothing() {
        EdgeIterator edgeIteratorNode2 = mock(EdgeIterator.class);
        when(edgeExplorer.setBaseNode(1)).thenReturn(edgeIterator);
        when(edgeExplorer.setBaseNode(2)).thenReturn(edgeIteratorNode2);
        when(edgeIterator.next()).thenReturn(true, false);
        when(edgeIterator.getEdge()).thenReturn(10);
        when(edgeIterator.getAdjNode()).thenReturn(2);
        when(edgeIterator.getDistance()).thenReturn(100.0);
        when(weighting.calcEdgeWeight(edgeIterator, false)).thenReturn(10.0);
        when(weighting.calcEdgeMillis(edgeIterator, false)).thenReturn(1000L);
        when(edgeIteratorNode2.next()).thenReturn(true, false);
        when(edgeIteratorNode2.getEdge()).thenReturn(20);
        when(edgeIteratorNode2.getAdjNode()).thenReturn(1);
        when(edgeIteratorNode2.getDistance()).thenReturn(50.0);
        when(weighting.calcEdgeWeight(edgeIteratorNode2, false)).thenReturn(5.0);
        when(weighting.calcEdgeMillis(edgeIteratorNode2, false)).thenReturn(500L);
        when(exploreLimit.isInLimit(any(RestrictionsIsochroneLabel.class), eq(encodingManager))).thenReturn(true);

        List<RestrictionsIsochroneLabel> visited = new ArrayList<>();
        restrictionIsochroneAlgorithm.search(1, visited::add);

        assertThat(visited).hasSize(2);
        assertThat(visited.get(0).getNode()).isEqualTo(1);
        assertThat(visited.get(1).getNode()).isEqualTo(2);
    }
}