package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.EdgeAccessHandler.isAccessible;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIteratorState;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;

@RequiredArgsConstructor
public class RoadChangesWeighting implements Weighting {

    private final Weighting sourceWeighting;

    private final NwbDataUpdates nwbDataUpdates;

    private final EncodingManager encodingManager;

    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Override
    public double calcMinWeightPerDistance() {
        return sourceWeighting.calcMinWeightPerDistance();
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeIteratorState, boolean reversedFlow) {
        int linkId = getLinkId(edgeIteratorState, encodingManager);
        return nwbDataUpdates.findChangedNwbRoadSectionById(linkId)
                .map(changedNwbRoadSection -> blockIfInaccessible(edgeIteratorState, reversedFlow, changedNwbRoadSection))
                .orElse(sourceWeighting.calcEdgeWeight(edgeIteratorState, reversedFlow));
    }

    private double blockIfInaccessible(
            EdgeIteratorState edgeIteratorState,
            boolean reversedFlow,
            AccessibilityNwbRoadSectionUpdate accessibilityNwbRoadSectionUpdate
    ) {
        boolean reversedDirection = edgeIteratorStateReverseExtractor.hasReversed(edgeIteratorState);
        return isAccessible(
                accessibilityNwbRoadSectionUpdate.carriagewayTypeCode(),
                accessibilityNwbRoadSectionUpdate.forwardAccessible(),
                accessibilityNwbRoadSectionUpdate.backwardAccessible(),
                reversedDirection)
                ? sourceWeighting.calcEdgeWeight(edgeIteratorState, reversedFlow)
                : Double.POSITIVE_INFINITY;
    }

    @Override
    public long calcEdgeMillis(EdgeIteratorState edgeIteratorState, boolean reversedFlow) {
        return sourceWeighting.calcEdgeMillis(edgeIteratorState, reversedFlow);
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
