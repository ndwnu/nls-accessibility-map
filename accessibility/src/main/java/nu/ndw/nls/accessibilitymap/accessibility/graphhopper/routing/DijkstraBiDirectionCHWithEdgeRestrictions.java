package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.routing;

import com.graphhopper.routing.DijkstraBidirectionCH;
import com.graphhopper.storage.RoutingCHEdgeIteratorState;
import com.graphhopper.storage.RoutingCHGraph;
import java.util.Set;

public class DijkstraBiDirectionCHWithEdgeRestrictions extends DijkstraBidirectionCH {

    private final Set<Integer> blockedEdges;

    public DijkstraBiDirectionCHWithEdgeRestrictions(RoutingCHGraph graph, Set<Integer> blockedEdges) {
        super(graph);
        this.blockedEdges = blockedEdges;
    }


    @Override
    protected double calcWeight(RoutingCHEdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        int edgeKey = reverse ? edgeState.getOrigEdgeKeyLast() : edgeState.getOrigEdgeKeyFirst();
        if (blockedEdges.contains(edgeKey)) {
            return Double.POSITIVE_INFINITY;

        }
        return super.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    }
}
