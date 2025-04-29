package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import static java.util.stream.Collectors.toCollection;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.index.Snap;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.TrafficSignSnap;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryGraphFactory {

    /**
     * Creates a QueryGraph based on a list of TrafficSignSnap objects and a starting point Snap. The QueryGraph is constructed by adding
     * virtual edges derived from the snaps, partitioning the underlying graph according to the fractional positions of the traffic signs.
     *
     * @param trafficSignSnaps the list of TrafficSignSnap objects representing the snaps of traffic signs
     * @param startPoint       the starting Snap point to be included in the graph
     * @return a QueryGraph instance with edges adjusted based on the given snaps
     */
    public QueryGraph createQueryGraph(NetworkGraphHopper networkGraphHopper, List<TrafficSignSnap> trafficSignSnaps, Snap startPoint) {

        List<Snap> snaps = trafficSignSnaps.stream()
                .map(TrafficSignSnap::getSnap)
                .collect(toCollection(ArrayList::new));
        snaps.add(startPoint);

        // The list of snaps will create virtual edges based on the snapped points, thereby cutting the affected edges
        // into 2-line strings. In this way, we create a graph that has an edge partitioning based on the fractional
        // positions of the traffic signs.
        // @see https://github.com/graphhopper/graphhopper/blob/master/docs/core/low-level-api.md#what-are-virtual-edges-and-nodes
        return QueryGraph.create(networkGraphHopper.getBaseGraph(), snaps);
    }

    /**
     * Constructs a QueryGraph by incorporating a list of TrafficSignSnap objects. The resulting graph includes virtual edges created based
     * on the snapped positions of the traffic signs. This process partitions the affected edges to represent the fractional positions of
     * the traffic signs within the graph.
     *
     * @param trafficSignSnaps the list of TrafficSignSnap objects containing snap information for traffic signs used to construct the
     *                         QueryGraph
     * @return a QueryGraph instance with modified edge structures reflecting the snapped positions
     */
    public QueryGraph createQueryGraph(List<TrafficSignSnap> trafficSignSnaps, NetworkGraphHopper networkGraphHopper) {

        List<Snap> snaps = trafficSignSnaps.stream()
                .map(TrafficSignSnap::getSnap)
                .collect(toCollection(ArrayList::new));

        // The list of snaps will create virtual edges based on the snapped points, thereby cutting the affected edges
        // into 2-line strings. In this way, we create a graph that has an edge partitioning based on the fractional
        // positions of the traffic signs.
        // @see https://github.com/graphhopper/graphhopper/blob/master/docs/core/low-level-api.md#what-are-virtual-edges-and-nodes
        return QueryGraph.create(networkGraphHopper.getBaseGraph(), snaps);
    }

}
