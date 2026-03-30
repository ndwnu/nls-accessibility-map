package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The {@code RestrictionWeightingDecorator} class is a decorator for the {@link Weighting} interface that imposes additional restrictions
 * on edge weights. It allows for blocking specific edges from being used during route calculations, by assigning them a weight of
 * {@code Double.POSITIVE_INFINITY}.
 * <p>
 * This class delegates actual weight calculations to a specified source {@link Weighting}, while overriding edge behaviours for edges that
 * are explicitly restricted. It can be used to modify routing logic dynamically based on conditions like avoiding certain paths or imposing
 * restrictions on specific segments of a network.
 * <p>
 * The restrictions are managed internally based on a set of blocked edge keys.
 * <p>
 * Core functionality: - Blocks specific edges by setting their weight to {@code Double.POSITIVE_INFINITY}. - Delegates weight and turn cost
 * computations to the source {@link Weighting}, except for blocked edges.
 */
@RequiredArgsConstructor
public class RestrictionWeightingDecorator implements Weighting {

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
    public double calcTurnWeight(int inEdge, int viaNode, int outEdge) {
        return sourceWeighting.calcTurnWeight(inEdge, viaNode, outEdge);
    }

    @Override
    public long calcTurnMillis(int inEdge, int viaNode, int outEdge) {
        return sourceWeighting.calcTurnMillis(inEdge, viaNode, outEdge);
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
