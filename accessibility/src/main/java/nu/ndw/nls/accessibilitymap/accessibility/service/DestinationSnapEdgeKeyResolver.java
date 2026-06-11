package nu.ndw.nls.accessibilitymap.accessibility.service;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DestinationSnapEdgeKeyResolver {

    public Optional<Integer> findEdgeKey(QueryGraph queryGraph, Snap destinationSnap, EncodingManager encodingManager) {
        EdgeExplorer edgeExplorer = queryGraph.createEdgeExplorer();
        int targetRoadSectionId = destinationSnap
                .getClosestEdge().get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
        EdgeIterator edgeIterator = edgeExplorer.setBaseNode(destinationSnap.getClosestNode());
        while (edgeIterator.next()) {
            int edgeRoadSectionId = edgeIterator.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
            if (edgeRoadSectionId == targetRoadSectionId) {
                return Optional.of(edgeIterator.getEdgeKey());
            }
        }
        return Optional.empty();
    }
}
