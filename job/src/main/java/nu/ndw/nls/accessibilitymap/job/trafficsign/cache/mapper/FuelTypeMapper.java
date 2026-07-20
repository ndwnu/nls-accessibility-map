package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import org.springframework.stereotype.Component;

@Component
public class FuelTypeMapper {

    @SuppressWarnings("java:S1172")
    public FuelType map(String fuelType) {
        // Traffic API V5 defines fuelTypes as a String, but the values are currently never set and the enumerated values are not available.
        // Returning null allows us to implement the fuel type logic without actually taking them into account. Not throwing an
        // IllegalArgumentException to prevent our API from breaking when fuel types do become available.
        return null;
    }
}
