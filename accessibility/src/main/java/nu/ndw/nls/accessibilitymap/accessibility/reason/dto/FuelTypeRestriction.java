package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import java.util.Set;
import lombok.experimental.SuperBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import org.apache.commons.collections4.CollectionUtils;

@SuperBuilder(toBuilder = true)
public class FuelTypeRestriction extends AccessibilityRestriction<Set<FuelType>> {

    private final Set<FuelType> value;

    @Override
    public RestrictionType getTypeOfRestriction() {

        return RestrictionType.FUEL_TYPE;
    }

    @Override
    public Set<FuelType> getValue() {

        return value;
    }

    @Override
    public boolean isEqual(AccessibilityRestriction<Set<FuelType>> other) {

        ensureSameType(other);

        return CollectionUtils.isEqualCollection(getValue(), other.getValue());
    }
}
