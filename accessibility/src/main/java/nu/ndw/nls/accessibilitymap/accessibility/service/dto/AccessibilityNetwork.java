package nu.ndw.nls.accessibilitymap.accessibility.service.dto;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.dto.RoadChanges;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
public class AccessibilityNetwork {

    @NotNull
    private final NetworkData networkData;

    @NotNull
    private final QueryGraph queryGraph;

    @NotNull
    private final Restrictions restrictions;

    @NotNull
    private final Map<Integer, List<Restriction>> restrictionsByEdgeKey;

    @NotNull
    private final Snap from;

    private final Snap destination;

    @NotNull
    private final Weighting weightingWithRestrictions;

    @NotNull
    private final Weighting weightingWithOutRestrictions;

    @NotNull
    private final RoadChanges roadChanges;

    @SuppressWarnings("java:S107")
    public AccessibilityNetwork(
            @NotNull NetworkData networkData,
            @NotNull RoadChanges roadChanges,
            @NotNull QueryGraph queryGraph,
            @NotNull Restrictions restrictions,
            @NotNull Map<Integer, List<Restriction>> restrictionsByEdgeKey,
            @NotNull Snap from,
            Snap destination,
            Weighting weightingWithRestrictions,
            Weighting weightingWithOutRestrictions
    ) {
        this.networkData = networkData;
        this.roadChanges = roadChanges;
        this.queryGraph = queryGraph;
        this.restrictions = restrictions;
        this.restrictionsByEdgeKey = restrictionsByEdgeKey;
        this.from = from;
        this.destination = destination;
        this.weightingWithRestrictions = weightingWithRestrictions;
        this.weightingWithOutRestrictions = weightingWithOutRestrictions;
    }

    @Override
    public String toString() {
        return "GraphHopperNetwork(" +
                "networkData=" + networkData + ", " +
                "restrictions=" + restrictions + ", " +
                "restrictionsByEdgeKey=" + restrictionsByEdgeKey + ", " +
                "from=" + from + ", " +
                "destination=" + destination +
                ')';
    }
}
