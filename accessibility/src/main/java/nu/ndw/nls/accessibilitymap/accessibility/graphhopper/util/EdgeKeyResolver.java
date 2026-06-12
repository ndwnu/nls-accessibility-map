package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import org.springframework.stereotype.Component;

@Component
public class EdgeKeyResolver {

    public int findForSnap(Snap snap, QueryGraph queryGraph, EncodingManager encodingManager) {
        int targetRoadSectionId = snap.getClosestEdge().get(encodingManager.getIntEncodedValue(WAY_ID_KEY));

        EdgeExplorer edgeExplorer = queryGraph.createEdgeExplorer();
        EdgeIterator edgeIterator = edgeExplorer.setBaseNode(snap.getClosestNode());
        while (edgeIterator.next()) {
            int edgeRoadSectionId = edgeIterator.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
            if (edgeRoadSectionId == targetRoadSectionId) {
                return edgeIterator.getEdgeKey();
            }
        }

        throw new IllegalStateException("A snap should always have an edge associated with an way id.");
    }
}
