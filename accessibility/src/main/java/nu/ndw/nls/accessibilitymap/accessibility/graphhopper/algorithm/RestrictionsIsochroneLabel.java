package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm;

import lombok.Getter;
import lombok.ToString;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.dto.IsochroneLabel;

@ToString(callSuper = true)
public class RestrictionsIsochroneLabel extends IsochroneLabel {

    @Getter
    private final Restrictions restrictions;

    public RestrictionsIsochroneLabel(
            int node,
            int edge,
            int edgeKey,
            RestrictionsIsochroneLabel parent,
            long time,
            double distance,
            double weight,
            Restrictions restrictions) {

        super(node, edge, edgeKey, parent, time, distance, weight);
        this.restrictions = restrictions;
    }
}
