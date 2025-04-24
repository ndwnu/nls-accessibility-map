package nu.ndw.nls.accessibilitymap.jobs.trafficsign.cache.mapper;

import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import org.springframework.stereotype.Component;

@Component
public class FuelTypeMapper {

    public Set<FuelType> map(nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.FuelType fuelType) {

        if (Objects.isNull(fuelType)) {
            return Set.of();
        }

        return switch (fuelType) {
            case ALL -> Set.of(FuelType.values());
            case BATTERY -> Set.of(FuelType.ELECTRIC);
            case BIODIESEL -> Set.of(FuelType.BIODIESEL);
            case DIESEL -> Set.of(FuelType.DIESEL);
            case DIESEL_BATTERY_HYBRID -> Set.of(FuelType.DIESEL, FuelType.ELECTRIC);
            case HYDROGEN -> Set.of(FuelType.HYDROGEN);
            case LPG, LIQUID_GAS -> Set.of(FuelType.LPG);
            case METHANE -> Set.of(FuelType.METHANE);
            case PETROL, PETROL_UNLEADED, PETROL_LEADED, PETROL_98_OCTANE, PETROL_95_OCTANE -> Set.of(FuelType.PETROL);
            case PETROL_BATTERY_HYBRID -> Set.of(FuelType.PETROL, FuelType.ELECTRIC);
            case ETHANOL, OTHER, UNKNOWN -> Set.of(FuelType.UNKNOWN);
        };
    }
}
