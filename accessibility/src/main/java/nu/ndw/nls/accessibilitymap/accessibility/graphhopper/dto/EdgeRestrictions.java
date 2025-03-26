package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;

/**
 * Represents a mapping of traffic signs and edge restrictions in a transportation network. The TrafficSignEdgeRestrictions class organizes
 * traffic signs by edge keys and tracks edges that are restricted or blocked.
 * <p>
 * This class is intended to process a list of {@code TrafficSignEdgeRestriction} objects, automatically organizing the data into two
 * primary structures: - A map that associates edge keys with their corresponding traffic signs. - A set of edge keys that represent blocked
 * or restricted edges.
 * <p>
 * The organization is achieved during initialization through the constructor using Java 8 stream operations to efficiently process the
 * input list.
 */
@Getter
public class EdgeRestrictions {

    private final Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey;

    private final Set<Integer> blockedEdges;

    public EdgeRestrictions(List<EdgeRestriction> edgeRestrictions) {

        this.blockedEdges = edgeRestrictions.stream()
                .map(EdgeRestriction::getEdgeKey)
                .collect(Collectors.toSet());

        this.trafficSignsByEdgeKey = edgeRestrictions.stream()
                .collect(groupingBy(
                        EdgeRestriction::getEdgeKey,
                        mapping(EdgeRestriction::getTrafficSign, toList())));
    }
}
