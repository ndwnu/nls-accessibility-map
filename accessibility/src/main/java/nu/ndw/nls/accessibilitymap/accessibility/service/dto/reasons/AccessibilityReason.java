package nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;

@Builder
public record AccessibilityReason(TrafficSign trafficSign, List<AccessibilityRestriction> restrictions) {

    /**
     * Compares the current AccessibilityReason with another AccessibilityReason to determine if the current instance is more restrictive.
     * Restrictions are compared based on their type and values, and the comparison assumes both AccessibilityReason objects pertain to the
     * same type of traffic sign.
     *
     * @param other the AccessibilityReason to compare to the current instance. Must have the same traffic sign type as the current
     *              instance.
     * @return true if the current AccessibilityReason imposes more restrictive conditions compared to the specified AccessibilityReason;
     * false otherwise.
     * @throws IllegalArgumentException if the traffic sign types of the two AccessibilityReason instances do not match.
     */
    public boolean isMoreRestrictiveThan(AccessibilityReason other) {
        if (!isSameTrafficSignType(other)) {
            throw new IllegalArgumentException("Traffic signs must be the same type to be compared.");
        }
        Map<RestrictionType, AccessibilityRestriction> otherRestrictionByType = other.restrictions.stream()
                .collect(toMap(AccessibilityRestriction::getTypeOfRestriction, Function.identity()));
        Map<RestrictionType, AccessibilityRestriction> thisRestrictionByType = restrictions.stream()
                .collect(toMap(AccessibilityRestriction::getTypeOfRestriction, Function.identity()));
        boolean isMoreRestrictive = false;
        for (RestrictionType type : thisRestrictionByType.keySet()) {
            AccessibilityRestriction thisRestriction = thisRestrictionByType.get(type);
            AccessibilityRestriction otherRestriction = otherRestrictionByType.get(type);
            isMoreRestrictive = otherRestriction == null || thisRestriction.isMoreRestrictiveThan(otherRestriction);
            if (isMoreRestrictive) {
                break;
            }

        }
        return isMoreRestrictive;
    }

    private boolean isSameTrafficSignType(AccessibilityReason other) {
        return trafficSign.trafficSignType() == other.trafficSign().trafficSignType();
    }
}
