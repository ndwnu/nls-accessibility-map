package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import org.junit.jupiter.api.Test;

class RestrictionWeightingAdapterTest {

    /**
     * Test class for RestrictionWeightingAdapter, specifically focused on testing the calcEdgeWeight method which calculates the weight of
     * an edge, considering its restrictions.
     */

    @Test
    void testCalcEdgeWeightWithBlockedEdge() {
        // Given
        Weighting mockSourceWeighting = mock(Weighting.class);
        Set<Integer> blockedEdges = Set.of(1, 2, 3);
        RestrictionWeightingAdapter adapter = new RestrictionWeightingAdapter(mockSourceWeighting, blockedEdges);

        EdgeIteratorState edgeIteratorState = mock(EdgeIteratorState.class);
        when(edgeIteratorState.getEdgeKey()).thenReturn(2); // Blocked edge

        // When
        double result = adapter.calcEdgeWeight(edgeIteratorState, false);

        // Then
        assertEquals(Double.POSITIVE_INFINITY, result,
                "calcEdgeWeight should return Double.POSITIVE_INFINITY for blocked edges");
    }

    @Test
    void testCalcEdgeWeightWithUnblockedEdge() {
        // Given
        Weighting mockSourceWeighting = mock(Weighting.class);
        Set<Integer> blockedEdges = Set.of(1, 2, 3);
        RestrictionWeightingAdapter adapter = new RestrictionWeightingAdapter(mockSourceWeighting, blockedEdges);

        EdgeIteratorState edgeIteratorState = mock(EdgeIteratorState.class);
        when(edgeIteratorState.getEdgeKey()).thenReturn(4); // Unblocked edge
        when(mockSourceWeighting.calcEdgeWeight(edgeIteratorState, false)).thenReturn(10.0);

        // When
        double result = adapter.calcEdgeWeight(edgeIteratorState, false);

        // Then
        assertEquals(10.0, result,
                "calcEdgeWeight should return the calculated weight from sourceWeighting for unblocked edges");
    }

    @Test
    void testCalcEdgeWeightWithReversed() {
        // Given
        Weighting mockSourceWeighting = mock(Weighting.class);
        Set<Integer> blockedEdges = Set.of(1, 2, 3);
        RestrictionWeightingAdapter adapter = new RestrictionWeightingAdapter(mockSourceWeighting, blockedEdges);

        EdgeIteratorState edgeIteratorState = mock(EdgeIteratorState.class);
        when(edgeIteratorState.getEdgeKey()).thenReturn(5); // Unblocked edge
        when(mockSourceWeighting.calcEdgeWeight(edgeIteratorState, true)).thenReturn(15.0);

        // When
        double result = adapter.calcEdgeWeight(edgeIteratorState, true);

        // Then
        assertEquals(15.0, result,
                "calcEdgeWeight should return the calculated weight from sourceWeighting, respecting the reversed flag for unblocked edges");
    }
}
