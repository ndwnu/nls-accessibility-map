package nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons;

public abstract class AccessibilityRestriction<VALUE_TYPE> {

    public enum RestrictionType {
        VEHICLE_LENGTH,
        VEHICLE_HEIGHT,
        VEHICLE_WIDTH,
        VEHICLE_WEIGHT,
        VEHICLE_AXLE_LOAD,
        FUEL_TYPE,
        VEHICLE_TYPE
    }


    public abstract RestrictionType getTypeOfRestriction();

    public abstract VALUE_TYPE getValue();

    public abstract boolean isMoreRestrictiveThan(AccessibilityRestriction<VALUE_TYPE> other);

    protected void ensureSameType(AccessibilityRestriction<VALUE_TYPE> other) {
        if (!getTypeOfRestriction().equals(other.getTypeOfRestriction())) {
            throw new IllegalArgumentException("Cannot compare accessibility restrictions of different types");
        }
    }
}
