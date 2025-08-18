package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;


import com.graphhopper.routing.querygraph.QueryGraph;
import java.util.Collection;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;

@Builder
public record NetworkData(
        NetworkGraphHopper networkGraphHopper,
        QueryGraph queryGraph,
        Collection<RoadSection> baseAccessibleRoads,
        EdgeRestrictions edgeRestrictions) {

}
