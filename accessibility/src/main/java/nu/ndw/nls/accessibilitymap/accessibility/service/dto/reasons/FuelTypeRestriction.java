package nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType.COMPRESSED_NATURAL_GAS;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType.DIESEL;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType.ELECTRIC;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType.ETHANOL;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType.HYDROGEN;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType.LIQUEFIED_NATURAL_GAS;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType.LIQUEFIED_PETROLEUM_GAS;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType.PETROL;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType.UNKNOWN;

import java.util.List;
import java.util.Set;
import lombok.Builder;
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
