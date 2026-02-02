package nu.ndw.nls.accessibilitymap.accessibility.service.dto;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.index.Snap;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
public class AccessibilityNetwork {

    @NotNull
    private final AccessibilityContext accessibilityContext;

    @NotNull
    private final QueryGraph queryGraph;

    @NotNull
    private final Restrictions restrictions;

    @NotNull
    private final Map<Integer, List<Restriction>> restrictionsByEdgeKey;

    @NotNull
    private final Set<Integer> blockedEdges;

    @NotNull
    private final Snap from;

    private final Snap destination;

    @SuppressWarnings("java:S107")
    public AccessibilityNetwork(
            @NotNull AccessibilityContext accessibilityContext,
            @NotNull QueryGraph queryGraph,
            @NotNull Restrictions restrictions,
            @NotNull Map<Integer, List<Restriction>> restrictionsByEdgeKey,
            @NotNull Snap from,
            Snap destination) {
        this.accessibilityContext = accessibilityContext;

        this.queryGraph = queryGraph;
        this.restrictions = restrictions;
        this.blockedEdges = new HashSet<>(restrictionsByEdgeKey.keySet());
        this.restrictionsByEdgeKey = restrictionsByEdgeKey;
        this.from = from;
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "GraphHopperNetwork[" +
               "restrictions=" + restrictions + ", " +
               "restrictionsByEdgeKey=" + restrictionsByEdgeKey + ", " +
               "blockedEdges=" + blockedEdges + ", " +
               "from=" + from + ", " +
               "destination=" + destination +
               ']';
    }
}
