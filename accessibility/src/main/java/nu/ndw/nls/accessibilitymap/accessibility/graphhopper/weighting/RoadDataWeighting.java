package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.EdgeAccessHandler.isAccessible;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;

@RequiredArgsConstructor
public class RoadDataWeighting implements Weighting {

    private final Weighting sourceWeighting;

    private final NwbData nwbData;

    private final EncodingManager encodingManager;

    @Override
    public double calcMinWeightPerDistance() {
        return sourceWeighting.calcMinWeightPerDistance();
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeIteratorState, boolean reversed) {
        int linkId = getLinkId(edgeIteratorState, encodingManager);

        return nwbData.findAccessibilityNwbRoadSectionById(linkId)
                .map(roadSection -> blockIfInaccessible(edgeIteratorState, reversed, roadSection))
                .orElseThrow(() -> new IllegalStateException("Road section not found for link id: " + linkId));
    }

    private double blockIfInaccessible(EdgeIteratorState edgeIteratorState, boolean reversed, AccessibilityNwbRoadSection roadSection) {
        return isAccessible(roadSection.carriagewayTypeCode(),
                roadSection.forwardAccessible(),
                roadSection.backwardAccessible(),
                reversed) ? sourceWeighting.calcEdgeWeight(edgeIteratorState, reversed) : Double.POSITIVE_INFINITY;
    }

    @Override
    public long calcEdgeMillis(EdgeIteratorState edgeIteratorState, boolean reversed) {
        return sourceWeighting.calcEdgeMillis(edgeIteratorState, reversed);
    }

    @Override
    public double calcTurnWeight(int inEdge, int viaNode, int outEdge) {
        return sourceWeighting.calcTurnWeight(inEdge, viaNode, outEdge);
    }

    @Override
    public long calcTurnMillis(int inEdge, int viaNode, int outEdge) {
        return sourceWeighting.calcTurnMillis(inEdge, viaNode, outEdge);
    }

    @Override
    public boolean hasTurnCosts() {
        return sourceWeighting.hasTurnCosts();
    }

    @Override
    public String getName() {
        return sourceWeighting.getName();
    }

    private static int getLinkId(EdgeIteratorState edge, EncodingManager encodingManager) {
        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }
}
