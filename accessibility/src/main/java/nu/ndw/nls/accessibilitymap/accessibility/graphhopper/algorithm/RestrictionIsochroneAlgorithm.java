package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.algorithm.AbstractDijkstraIsochroneAlgorithm;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.exploration.ExploreLimit;

@Slf4j
public class RestrictionIsochroneAlgorithm extends AbstractDijkstraIsochroneAlgorithm<RestrictionsIsochroneLabel> {

    private final Map<Integer, List<Restriction>> restrictionsByEdgeKey;

    @SuppressWarnings("java:S107")
    public RestrictionIsochroneAlgorithm(
            Graph graph,
            EncodingManager encodingManager,
            TraversalMode traversalMode,
            boolean traversalInReverseFlow,
            Weighting weighting,
            ExploreLimit<RestrictionsIsochroneLabel> exploreLimit,
            Comparator<RestrictionsIsochroneLabel> explorePriorityComparator,
            Map<Integer, List<Restriction>> restrictionsByEdgeKey) {

        super(graph, encodingManager, traversalMode, traversalInReverseFlow, weighting, exploreLimit, explorePriorityComparator);

        this.restrictionsByEdgeKey = restrictionsByEdgeKey;
    }

    @Override
    protected RestrictionsIsochroneLabel createNewIsoLabel(
            int node,
            int edge,
            int edgeKey,
            RestrictionsIsochroneLabel parent,
            long time,
            double distance,
            double weight,
            EncodingManager encodingManager) {

        return new RestrictionsIsochroneLabel(
                node,
                edge,
                edgeKey,
                parent,
                time,
                distance,
                weight,
                new Restrictions(restrictionsByEdgeKey.getOrDefault(edgeKey, List.of())));
    }

    @Override
    protected void mergeEqualWeightedIsoLabels(RestrictionsIsochroneLabel target, RestrictionsIsochroneLabel source) {

        // Nothing to merge in our case
    }
}
