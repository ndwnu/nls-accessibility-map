package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;

public final class AccessibilityReasons {

    private final Map<Integer, Map<Direction, List<AccessibilityReason>>> reasonsByRoadSectionId;

    public AccessibilityReasons(List<AccessibilityReason> reasons) {

        this.reasonsByRoadSectionId = reasons.stream()
                .collect(groupingBy(AccessibilityReason::roadSectionId,
                        groupingBy(AccessibilityReason::direction)));
    }

    public List<AccessibilityReason> getReasonsByRoadSectionAndDirection(int roadSectionId, Direction direction) {

        return reasonsByRoadSectionId.getOrDefault(roadSectionId, Map.of())
                .getOrDefault(direction, List.of());
    }

    public boolean hasReasons(int roadSectionId, Direction direction) {

        return reasonsByRoadSectionId.containsKey(roadSectionId) && reasonsByRoadSectionId.get(roadSectionId).containsKey(direction);
    }
}
