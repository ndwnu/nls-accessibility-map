package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;


import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.TrafficSignEdgeRestrictions;

/**
 * The RestrictionWeightingAdapter is a wrapper class that adapts a {@link Weighting} implementation
 * to account for edge restrictions provided by {@link TrafficSignEdgeRestrictions}. This adapter modifies
 * the behavior of edge weighting calculations by checking if an edge is restricted based on the
 * provided restrictions. If an edge is restricted, it assigns a weight of {@code Double.POSITIVE_INFINITY}
 * to that edge, effectively making it inaccessible during routing computations.
 * <p>
 * This class delegates all other weighting calculations (e.g., turn costs, edge millis) to the
 * underlying {@link Weighting} instance. It ensures compatibility with existing weight-based
 * routing algorithms while incorporating custom edge restrictions.
 */
@RequiredArgsConstructor
public class RestrictionWeightingAdapter implements Weighting {

    private final Weighting sourceWeighting;
    private final TrafficSignEdgeRestrictions edgeRestrictions;

    @Override
    public double calcMinWeightPerDistance() {
        return sourceWeighting.calcMinWeightPerDistance();
    }

    /**
     * Calculates the weight of the specified edge, possibly considering a restriction that
     * renders the edge inaccessible. If the edge is restricted, the weight is assigned
     * as {@code Double.POSITIVE_INFINITY}; otherwise, the calculation is delegated to
     * the underlying weighting implementation.
     *
     * @param edgeIteratorState the state of the edge for which the weight is being calculated
     * @param reversed a boolean indicating whether the edge direction should be reversed
     * @return the calculated weight of the edge; {@code Double.POSITIVE_INFINITY} if the edge
     *         is restricted, otherwise the weight calculated by the underlying weighting
     */
    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeIteratorState, boolean reversed) {
        if (edgeRestrictions.hasEdgeRestrictions(edgeIteratorState.getEdgeKey())) {
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
