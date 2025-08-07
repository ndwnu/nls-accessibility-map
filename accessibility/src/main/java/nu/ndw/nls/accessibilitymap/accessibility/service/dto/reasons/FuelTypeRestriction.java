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
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;

@Builder
public class FuelTypeRestriction extends AccessibilityRestriction<Set<FuelType>> {

    private static List<FuelType> FUEL_TYPES = List.of(ELECTRIC, HYDROGEN,
            ETHANOL,
            COMPRESSED_NATURAL_GAS,
            LIQUEFIED_PETROLEUM_GAS,
            LIQUEFIED_NATURAL_GAS,
            PETROL,
            DIESEL, UNKNOWN);
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
    public boolean isMoreRestrictiveThan(AccessibilityRestriction<Set<FuelType>> other) {
        ensureSameType(other);
        int sumIndex = getValue().stream().mapToInt(FUEL_TYPES::indexOf).sum();
        int otherSumIndex = other.getValue().stream().mapToInt(FUEL_TYPES::indexOf).sum();
        return sumIndex < otherSumIndex;
    }
}
