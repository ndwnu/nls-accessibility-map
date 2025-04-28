package nu.ndw.nls.accessibilitymap.accessibility.core.dto;


import com.graphhopper.routing.querygraph.QueryGraph;
import java.util.Collection;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.EdgeRestrictions;

@Builder
public record NetworkData(QueryGraph queryGraph, Collection<RoadSection> baseAccessibleRoads,
                          EdgeRestrictions edgeRestrictions) {

}
