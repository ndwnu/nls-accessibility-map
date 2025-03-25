package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.TrafficSignEdgeRestrictions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RestrictionWeightingAdapterTest {

    private static final int EDGE_KEY = 1;

    /**
     * Unit tests for the `calcEdgeWeight` method in the `RestrictionWeightingAdapter` class. The `calcEdgeWeight` method calculates the
     * weight of an edge while taking into consideration restrictions defined in `TrafficSignEdgeRestrictions`. If the edge has
     * restrictions, the method returns `Double.POSITIVE_INFINITY`, otherwise it delegates to the source `Weighting` implementation.
     */

    @Test
    void testCalcEdgeWeight_WithEdgeRestrictions() {
        TrafficSignEdgeRestrictions restrictions = Mockito.mock(TrafficSignEdgeRestrictions.class);
        Weighting sourceWeighting = Mockito.mock(Weighting.class);
        EdgeIteratorState edgeIteratorState = Mockito.mock(EdgeIteratorState.class);

        when(edgeIteratorState.getEdgeKey()).thenReturn(EDGE_KEY);
        when(restrictions.hasEdgeRestrictions(EDGE_KEY)).thenReturn(true);

        RestrictionWeightingAdapter adapter = new RestrictionWeightingAdapter(sourceWeighting, restrictions);
        double result = adapter.calcEdgeWeight(edgeIteratorState, false);

        assertEquals(Double.POSITIVE_INFINITY, result);
        verify(restrictions, times(1)).hasEdgeRestrictions(EDGE_KEY);
        verifyNoInteractions(sourceWeighting);
    }

    @Test
    void testCalcEdgeWeight_WithoutEdgeRestrictions() {
        TrafficSignEdgeRestrictions restrictions = Mockito.mock(TrafficSignEdgeRestrictions.class);
        Weighting sourceWeighting = Mockito.mock(Weighting.class);
        EdgeIteratorState edgeIteratorState = Mockito.mock(EdgeIteratorState.class);

        when(edgeIteratorState.getEdgeKey()).thenReturn(EDGE_KEY);
        when(restrictions.hasEdgeRestrictions(EDGE_KEY)).thenReturn(false);
        when(sourceWeighting.calcEdgeWeight(edgeIteratorState, false)).thenReturn(10.0);

        RestrictionWeightingAdapter adapter = new RestrictionWeightingAdapter(sourceWeighting, restrictions);
        double result = adapter.calcEdgeWeight(edgeIteratorState, false);

        assertEquals(10.0, result);
        verify(restrictions, times(1)).hasEdgeRestrictions(EDGE_KEY);
        verify(sourceWeighting, times(1)).calcEdgeWeight(edgeIteratorState, false);
    }
}
