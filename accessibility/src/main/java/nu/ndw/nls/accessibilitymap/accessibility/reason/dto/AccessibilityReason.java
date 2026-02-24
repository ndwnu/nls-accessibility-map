package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;

@SuperBuilder
@NoArgsConstructor
public abstract class AccessibilityReason<VALUE_TYPE> {

    public enum ReasonType {
        VEHICLE_LENGTH,
        VEHICLE_HEIGHT,
        VEHICLE_WIDTH,
        VEHICLE_WEIGHT,
        VEHICLE_AXLE_LOAD,
        FUEL_TYPE,
        VEHICLE_TYPE,
        ACCESSIBLE_REASON
    }

    @Getter
    /*
      The causes of why this reason exists.
     */
    private Set<Restriction> restrictions;

    public abstract ReasonType getReasonType();

    public abstract VALUE_TYPE getValue();

    @SuppressWarnings("unchecked")
    protected AccessibilityReason<VALUE_TYPE> ensureSameType(AccessibilityReason<?> other) {
        if (getReasonType() != other.getReasonType()) {
            throw new IllegalArgumentException("Cannot compare accessibility restrictions of different types");
        }

        return (AccessibilityReason<VALUE_TYPE>) other;
    }

    public abstract AccessibilityReason<VALUE_TYPE> reduce(AccessibilityReason<?> other);

    protected Set<Restriction> mergeRestrictions(AccessibilityReason<VALUE_TYPE> otherReason) {
        return Stream.concat(
                        getRestrictions().stream(),
                        otherReason.getRestrictions().stream())
                .collect(Collectors.toSet());
    }
}
