package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EdgeKeyResolverTest {

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private EdgeExplorer edgeExplorer;

    @Mock
    private EdgeIterator edgeIterator;

    @Mock
    private Snap snap;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private IntEncodedValue idIntEncodedValue;

    @Mock
    private EdgeIteratorState endSegmentClosestEdge;

    private EdgeKeyResolver edgeKeyResolver;

    @BeforeEach
    void setUp() {
        edgeKeyResolver = new EdgeKeyResolver();
    }

    @Test
    void findForSnap() {
        int closestNode = 1;
        int edgeKey = 15;

        when(queryGraph.createEdgeExplorer()).thenReturn(edgeExplorer);
        when(edgeExplorer.setBaseNode(closestNode)).thenReturn(edgeIterator);

        when(snap.getClosestEdge()).thenReturn(endSegmentClosestEdge);
        when(endSegmentClosestEdge.get(idIntEncodedValue)).thenReturn(123);
        when(snap.getClosestNode()).thenReturn(closestNode);
        when(encodingManager.getIntEncodedValue("way_id")).thenReturn(idIntEncodedValue);

        when(edgeIterator.next())
                .thenReturn(true)
                .thenReturn(true);
        when(edgeIterator.get(idIntEncodedValue))
                .thenReturn(111)
                .thenReturn(123);
        when(edgeIterator.getEdgeKey()).thenReturn(edgeKey);

        int foundEdgeKey = edgeKeyResolver.findForSnap(snap, queryGraph, encodingManager);

        assertThat(foundEdgeKey).isEqualTo(edgeKey);

        verify(edgeIterator).getEdgeKey();
    }

    @Test
    void findForSnap_noEdgeKeyFoundForSnap() {
        int closestNode = 1;

        when(queryGraph.createEdgeExplorer()).thenReturn(edgeExplorer);
        when(edgeExplorer.setBaseNode(closestNode)).thenReturn(edgeIterator);

        when(snap.getClosestEdge()).thenReturn(endSegmentClosestEdge);
        when(endSegmentClosestEdge.get(idIntEncodedValue)).thenReturn(123);
        when(snap.getClosestNode()).thenReturn(closestNode);
        when(encodingManager.getIntEncodedValue("way_id")).thenReturn(idIntEncodedValue);

        when(edgeIterator.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(edgeIterator.get(idIntEncodedValue))
                .thenReturn(111)
                .thenReturn(222);

        assertThatThrownBy(() -> edgeKeyResolver.findForSnap(snap, queryGraph, encodingManager))
                .hasMessage("A snap should always have an edge associated with an way id.");
    }
}
