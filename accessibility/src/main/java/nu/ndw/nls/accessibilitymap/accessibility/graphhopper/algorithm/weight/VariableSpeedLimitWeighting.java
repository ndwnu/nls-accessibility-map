package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.weight;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIteratorState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.speedlimit.SpeedLimit;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto.SpeedLimits;

@Slf4j
@RequiredArgsConstructor
public class VariableSpeedLimitWeighting implements Weighting {

    private static final double MILLI_SECONDS_PER_HOUR = 3_600_000;

    private static final double METERS_PER_KILOMETER = 1000;

    private final Weighting sourceWeighting;

    private final SpeedLimits speedLimits;

    private final EncodingManager encodingManager;

    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Override
    public double calcMinWeightPerDistance() {
        return sourceWeighting.calcMinWeightPerDistance();
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeIteratorState, boolean reversedFlow) {

        return sourceWeighting.calcEdgeWeight(edgeIteratorState, reversedFlow);
    }

    @Override
    public long calcEdgeMillis(EdgeIteratorState edgeIteratorState, boolean reverseSearch) {
        int roadSectionId = getRoadSectionId(edgeIteratorState);
        boolean edgeIsReversed = edgeIteratorStateReverseExtractor.hasReversed(edgeIteratorState);
        // see docs/technical-details.md ## Bidirectional routing and edge orientation in combination with search direction
        boolean traversingBackwardRelativeToRoad = reverseSearch ^ edgeIsReversed;
        Direction direction = traversingBackwardRelativeToRoad ? Direction.BACKWARD : Direction.FORWARD;
        return speedLimits.findByRoadSectionId(roadSectionId, direction)
                .map(speedLimit -> calculateTraversalTimeInMilliSeconds(edgeIteratorState.getDistance(), speedLimit))
                .orElse(sourceWeighting.calcEdgeMillis(edgeIteratorState, reverseSearch));
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

    private int getRoadSectionId(EdgeIteratorState edgeIteratorState) {
        return edgeIteratorState.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }

    private long calculateTraversalTimeInMilliSeconds(double distanceInMeters, SpeedLimit speedLimit) {

        double activeSpeedLimit = speedLimit.speedInKmPerHour();
        return Math.round(distanceInMeters / METERS_PER_KILOMETER / activeSpeedLimit * MILLI_SECONDS_PER_HOUR);
    }
}
