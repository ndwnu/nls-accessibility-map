package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.weight;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIteratorState;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util.IsCarAccessibleUtil;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NwbNetworkData;

@RequiredArgsConstructor
public class CarAccessibleRoadsWeighting implements Weighting {

    private final Weighting sourceWeighting;

    private final NwbNetworkData nwbNetworkData;

    private final EncodingManager encodingManager;

    public final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Override
    public double calcMinWeightPerDistance() {
        return sourceWeighting.calcMinWeightPerDistance();
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
        int roadSectionId = getRoadSectionId(edgeState, encodingManager);
        boolean travellingInReversedDirection = edgeIteratorStateReverseExtractor.hasReversed(edgeState);
        return nwbNetworkData.findAccessibilityNwbRoadSectionById(roadSectionId).stream()
                .allMatch(accessibilityNwbRoadSection ->
                        IsCarAccessibleUtil.isAccessible(accessibilityNwbRoadSection, travellingInReversedDirection)) ?
                sourceWeighting.calcEdgeWeight(edgeState, reverse) : Double.POSITIVE_INFINITY;
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

    private static int getRoadSectionId(EdgeIteratorState edge, EncodingManager encodingManager) {
        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }
}
