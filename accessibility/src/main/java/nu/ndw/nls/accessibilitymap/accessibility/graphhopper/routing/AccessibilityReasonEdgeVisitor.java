package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.routing;

import static java.util.stream.Collectors.groupingBy;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.Path.EdgeVisitor;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIteratorState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReasons;

@Slf4j
@RequiredArgsConstructor
public class AccessibilityReasonEdgeVisitor implements EdgeVisitor {

    private final AccessibilityReasons accessibilityReasons;
    private final EncodingManager encodingManager;
    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;
    @Getter
    private List<AccessibilityReason> accessibilityReasonList = new ArrayList<>();

    private final Map<TrafficSignType, List<AccessibilityReason>> reasonsByType = new HashMap<>();

    @Override
    public void next(EdgeIteratorState edgeIteratorState, int index, int prevEdgeId) {
        int linkId = getLinkId(encodingManager, edgeIteratorState);
        Direction direction = edgeIteratorStateReverseExtractor.hasReversed(edgeIteratorState) ? Direction.BACKWARD : Direction.FORWARD;
        if (accessibilityReasons.hasReasons(linkId, direction)) {
            accessibilityReasons.getReasonsByRoadSectionAndDirection(linkId, direction).stream()
                    .collect(groupingBy(r -> r.trafficSign().trafficSignType()))
                    .forEach((key, value) -> reasonsByType.merge(key, value, (one, two) -> {
                        one.addAll(two);
                        return one;
                    }));
        }

    }

    @Override
    public void finish() {
        accessibilityReasonList = reasonsByType.values().stream().map(this::getMostRestrictiveReason).toList();
    }

    private AccessibilityReason getMostRestrictiveReason(List<AccessibilityReason> reasons) {
        return reasons.stream()
                .reduce((one, two) ->
                        one.isMoreRestrictiveThan(two) ? one : two)
                .orElseThrow(() -> new IllegalStateException("Reasons list is empty"));
    }

    private int getLinkId(EncodingManager encodingManager, EdgeIteratorState edge) {

        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }
}
