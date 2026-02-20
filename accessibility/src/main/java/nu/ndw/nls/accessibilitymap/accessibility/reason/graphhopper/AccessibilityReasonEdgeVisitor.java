package nu.ndw.nls.accessibilitymap.accessibility.reason.graphhopper;

import com.graphhopper.routing.Path.EdgeVisitor;
import com.graphhopper.util.EdgeIteratorState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.mapper.RestrictionMapper;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessibilityReasonEdgeVisitor implements EdgeVisitor {

    private final Map<Integer, DirectionalSegment> directionalSegmentsById;

    private final List<RestrictionMapper> restrictionMappers;

    private final List<AccessibilityReason<?>> collectedReasons = new ArrayList<>();

    @Getter
    private final List<AccessibilityReason<?>> reasons = new ArrayList<>();

    @Getter
    private final List<DirectionalSegment> pathFollowed = new ArrayList<>();

    public static AccessibilityReasonEdgeVisitor create(Map<Integer, DirectionalSegment> directionalSegmentsById, List<RestrictionMapper> restrictionMappers) {
        return new AccessibilityReasonEdgeVisitor(directionalSegmentsById, restrictionMappers);
    }

    @Override
    public void next(EdgeIteratorState edgeIteratorState, int index, int prevEdgeId) {

        int directionId = edgeIteratorState.getEdgeKey();

        DirectionalSegment directionalSegment = directionalSegmentsById.get(directionId);
        pathFollowed.add(directionalSegment);

        collectedReasons.addAll(restrictionMappers.stream()
                .map(restrictionMapper -> restrictionMapper.mapRestrictions(directionalSegment.getRestrictions()))
                .flatMap(List::stream)
                .toList());
    }

    @Override
    public void finish() {

        Map<ReasonType, List<AccessibilityReason<?>>> reasonsByType = collectedReasons.stream()
                .collect(Collectors.groupingBy(AccessibilityReason::getReasonType));

        var reasonsToAdd = reasonsByType.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> {
                    AccessibilityReason<?> reducedReason = reduceToOneReason(entry.getValue());

                    log.debug("Reduced reason type {} to {}", entry.getKey(), reducedReason);

                    return reducedReason;
                })
                .toList();

        reasons.addAll(reasonsToAdd);
    }

    private static AccessibilityReason<?> reduceToOneReason(List<AccessibilityReason<?>> reasonsToReduce) {
        AccessibilityReason<?> currentReason = null;
        for (AccessibilityReason<?> reasonToReduce : reasonsToReduce) {
            if (currentReason == null) {
                currentReason = reasonToReduce;
                continue;
            }

            currentReason = currentReason.reduce(reasonToReduce);
        }
        return currentReason;
    }
}
