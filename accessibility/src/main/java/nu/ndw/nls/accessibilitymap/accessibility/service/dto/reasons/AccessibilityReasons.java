package nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons;

import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;


public class AccessibilityReasons {

    private final Map<Integer, Map<Direction, Map<TrafficSignType, List<AccessibilityRestriction>>>> restrictionsByRoadSectionId;
    private final Map<Integer, Map<Direction, List<AccessibilityReason>>> reasonsByRoadSectionId;

    private AccessibilityReasons(List<AccessibilityReason> reasons) {
        this.reasonsByRoadSectionId = reasons.stream()
                .collect(groupingBy(r -> r.trafficSign().roadSectionId(),
                        groupingBy(r -> r.trafficSign().direction())));

        this.restrictionsByRoadSectionId = reasons
                .stream()
                .collect(groupingBy(r -> r.trafficSign().roadSectionId(),
                        groupingBy(r -> r.trafficSign().direction(),
                                groupingBy(r -> r.trafficSign().trafficSignType(),
                                        mapping(AccessibilityReason::restrictions,
                                                flatMapping(List::stream, Collectors.toList()))))));
    }

    public List<AccessibilityReason> getReasonsByRoadSectionAndDirection(int roadSectionId, Direction direction) {
        return reasonsByRoadSectionId.get(roadSectionId)
                .get(direction);
    }

    public boolean hasReasons(int roadSectionId, Direction direction) {
        return reasonsByRoadSectionId.containsKey(roadSectionId) && reasonsByRoadSectionId.get(roadSectionId).containsKey(direction);
    }


    public static AccessibilityReasons of(List<AccessibilityReason> reasons) {
        return new AccessibilityReasons(reasons);
    }
}
