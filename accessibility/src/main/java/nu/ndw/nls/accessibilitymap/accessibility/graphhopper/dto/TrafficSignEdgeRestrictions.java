package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;

/**
 * The TrafficSignEdgeRestrictions class maintains a mapping between edge keys and their associated
 * traffic sign restrictions. This class is used to query and retrieve traffic sign-related restrictions
 * for specific edges in a transportation network.
 *
 * The restrictions are provided as a list of TrafficSignEdgeRestriction objects,
 * which are converted into an internal map using the edge key as the key and the corresponding
 * TrafficSignEdgeRestriction as the value.
 */
@Getter
public class TrafficSignEdgeRestrictions {

    private final Map<Integer, TrafficSignEdgeRestriction> restrictions;

    public TrafficSignEdgeRestrictions(List<TrafficSignEdgeRestriction> restrictions) {
        this.restrictions = restrictions.stream()
                .collect(toMap(TrafficSignEdgeRestriction::getEdgeKey, Function.identity()));
    }

    public boolean hasEdgeRestriction(int edgeKey) {
        return restrictions.containsKey(edgeKey);
    }

    public TrafficSignEdgeRestriction getEdgeRestriction(int edgeKey) {
        return restrictions.get(edgeKey);
    }

    public static TrafficSignEdgeRestrictions emptyRestrictions() {
        return new TrafficSignEdgeRestrictions(List.of());
    }

}
