package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util.EdgeAccessHandler.isAccessible;
import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util.LinkIdResolver.resolveLinkId;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.dto.ChangedNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.dto.RoadChanges;

@RequiredArgsConstructor
public class RoadChangesWeightingDecorator implements Weighting {

    private final Weighting sourceWeighting;

    private final RoadChanges roadChanges;

    private final EncodingManager encodingManager;

    @Override
    public double calcMinWeightPerDistance() {
        return sourceWeighting.calcMinWeightPerDistance();
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeIteratorState, boolean reversed) {
        int linkId = resolveLinkId(edgeIteratorState, encodingManager, reversed);
        return roadChanges.findChangedNwbRoadSectionById(linkId)
                .map(changedNwbRoadSection -> blockIfInaccessible(edgeIteratorState, reversed, changedNwbRoadSection))
                .orElse(sourceWeighting.calcEdgeWeight(edgeIteratorState, reversed));
    }

    private double blockIfInaccessible(EdgeIteratorState edgeIteratorState, boolean reversed, ChangedNwbRoadSection changedNwbRoadSection) {
        return isAccessible(changedNwbRoadSection.carriagewayTypeCode(),
                changedNwbRoadSection.forwardAccessible(),
                changedNwbRoadSection.backwardAccessible(),
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
}
