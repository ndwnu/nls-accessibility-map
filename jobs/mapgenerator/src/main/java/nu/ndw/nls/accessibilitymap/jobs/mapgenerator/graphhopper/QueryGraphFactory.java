package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import static java.util.stream.Collectors.toCollection;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.index.Snap;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.TrafficSignSnap;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryGraphFactory {

    private final QueryGraphConfigurer queryGraphConfigurer;
    private final NetworkGraphHopper networkGraphHopper;

    public QueryGraph createQueryGraph(List<TrafficSignSnap> trafficSignSnaps, Snap startPoint) {
        List<Snap> snaps = trafficSignSnaps
                .stream()
                .map(TrafficSignSnap::getSnap)
                .collect(toCollection(ArrayList::new));
        snaps.add(startPoint);
        // The list of snaps will create virtual edges based on the snapped points, thereby cutting the affected edges into 2-line strings.
        // In this way, we create a graph that has an edge partitioning based on the fractional positions of the traffic signs
        // @see https://github.com/graphhopper/graphhopper/blob/master/docs/core/low-level-api.md#what-are-virtual-edges-and-nodes
        QueryGraph queryGraph = QueryGraph.create(networkGraphHopper.getBaseGraph(), snaps);
        // Here the snapped traffic signs are assigned to the correct edges by determining if the edge is behind a traffic sign.
        queryGraphConfigurer.configure(queryGraph, trafficSignSnaps);
        return queryGraph;
    }
}
