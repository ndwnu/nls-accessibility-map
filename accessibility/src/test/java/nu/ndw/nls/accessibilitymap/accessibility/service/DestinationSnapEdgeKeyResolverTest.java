package nu.ndw.nls.accessibilitymap.accessibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DestinationSnapEdgeKeyResolverTest {

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private EdgeExplorer edgeExplorer;

    @Mock
    private EdgeIterator edgeIterator;

    @Mock
    private Snap destinationSnap;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private IntEncodedValue idIntEncodedValue;

    @Mock
    private EdgeIteratorState endSegmentClosestEdge;

    private DestinationSnapEdgeKeyResolver destinationSnapEdgeKeyResolver;

    @BeforeEach
    void setUp() {
        destinationSnapEdgeKeyResolver = new DestinationSnapEdgeKeyResolver();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            30,30
            30,31
            """)
    void findEdgeKey(int edgeRoadSectionId, int targetRoadSectionId) {
        int closestNode = 1;
        int edgeKey = 15;
        when(queryGraph.createEdgeExplorer()).thenReturn(edgeExplorer);
        when(destinationSnap.getClosestEdge())
                .thenReturn(endSegmentClosestEdge);
        when(endSegmentClosestEdge.get(idIntEncodedValue)).thenReturn(closestNode);
        when(destinationSnap.getClosestNode()).thenReturn(closestNode);
        when(edgeExplorer.setBaseNode(closestNode)).thenReturn(edgeIterator);
        when(edgeIterator.next())
                .thenReturn(true)
                .thenReturn(false);
        when(endSegmentClosestEdge.get(idIntEncodedValue)).thenReturn(edgeRoadSectionId);
        when(encodingManager.getIntEncodedValue("way_id")).thenReturn(idIntEncodedValue);
        when(edgeIterator.get(idIntEncodedValue)).thenReturn(targetRoadSectionId);
        if (edgeRoadSectionId == targetRoadSectionId) {
            when(edgeIterator.getEdgeKey()).thenReturn(edgeKey);
        }

        Optional<Integer> maybeEdgeKey = destinationSnapEdgeKeyResolver.findEdgeKey(queryGraph, destinationSnap, encodingManager);

        if (edgeRoadSectionId == targetRoadSectionId) {
            assertThat(maybeEdgeKey).contains(edgeKey);
        } else {
            assertThat(maybeEdgeKey).isEmpty();
        }
    }
}
