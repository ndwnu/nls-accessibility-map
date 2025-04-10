package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Adapter class that wraps a {@link Weighting} instance and provides additional functionality for blocking specific edges during routing.
 * The {@code blockedEdges} set contains edge keys that are considered restricted, and such edges will be assigned an infinite weight to
 * make them unavailable for routing.
 */
@RequiredArgsConstructor
public class RestrictionWeightingAdapter implements Weighting {

    private final Weighting sourceWeighting;

    @Getter
    private final Set<Integer> blockedEdges;

    @Override
    public double calcMinWeightPerDistance() {
        return sourceWeighting.calcMinWeightPerDistance();
    }

    /**
     * Calculates the weight of a given edge, taking into account any restrictions imposed by the {@code blockedEdges} set. If the edge is
     * restricted, the method returns {@code Double.POSITIVE_INFINITY}, rendering the edge unusable for routing purposes. Otherwise,
     * delegates the weight calculation to the underlying {@code sourceWeighting}.
     *
     * @param edgeIteratorState the edge for which the weight is being calculated
     * @param reversed          indicates whether the edge direction should be reversed during the calculation
     * @return the calculated weight of the edge, or {@code Double.POSITIVE_INFINITY} if the edge is restricted
     */
    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeIteratorState, boolean reversed) {
        if (blockedEdges.contains(edgeIteratorState.getEdgeKey())) {
            return Double.POSITIVE_INFINITY;
        }
        return sourceWeighting.calcEdgeWeight(edgeIteratorState, reversed);
    }

    @Override
    public long calcEdgeMillis(EdgeIteratorState edgeIteratorState, boolean reversed) {
        return sourceWeighting.calcEdgeMillis(edgeIteratorState, reversed);
    }

    @Override
    public double calcTurnWeight(int i, int i1, int i2) {
        return sourceWeighting.calcTurnWeight(i, i1, i2);
    }

    @Override
    public long calcTurnMillis(int i, int i1, int i2) {
        return sourceWeighting.calcTurnMillis(i, i1, i2);
    }

    @Override
    public boolean hasTurnCosts() {
        return sourceWeighting.hasTurnCosts();
    }

    @Override
    public String getName() {
        return sourceWeighting.getName();
    }
}
