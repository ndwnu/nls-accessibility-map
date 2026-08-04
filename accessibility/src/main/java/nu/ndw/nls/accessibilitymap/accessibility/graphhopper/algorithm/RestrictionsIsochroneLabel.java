package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm;

import java.util.Objects;
import lombok.Getter;
import lombok.ToString;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.dto.IsochroneLabel;

@ToString(callSuper = true)
public class RestrictionsIsochroneLabel extends IsochroneLabel {

    @Getter
    private final Restrictions restrictions;

    @Getter
    private final boolean traversalInReverseFlow;

    @Getter
    private final boolean edgeIsReversed;

    @SuppressWarnings("java:S107")
    public RestrictionsIsochroneLabel(
            int node,
            int edge,
            int edgeKey,
            RestrictionsIsochroneLabel parent,
            long time,
            double distance,
            double weight,
            Restrictions restrictions,
            boolean traversalInReverseFlow,
            boolean edgeIsReversed
    ) {

        super(node, edge, edgeKey, parent, time, distance, weight);
        this.restrictions = restrictions;
        this.traversalInReverseFlow = traversalInReverseFlow;
        this.edgeIsReversed = edgeIsReversed;
    }

    public boolean hasRestrictions() {
        return Objects.nonNull(restrictions) && !restrictions.isEmpty();
    }

    /**
     * Determines if the traversal direction is backward relative to the road's natural orientation.
     * <p>
     * It compares whether the traversal is in reverse flow and if the edge is reversed. The result is based on an exclusive OR (XOR)
     * operation between these two states. see docs/technical-details.md ## Bidirectional routing and edge orientation in combination with
     * search direction
     *
     * @return true if traversing is backward relative to the road's natural orientation; false otherwise.
     */
    public boolean isTraversingBackwardRelativeToRoad() {
        return traversalInReverseFlow ^ edgeIsReversed;
    }
}
