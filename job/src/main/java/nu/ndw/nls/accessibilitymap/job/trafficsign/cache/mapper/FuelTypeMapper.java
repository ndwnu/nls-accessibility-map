package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import org.springframework.stereotype.Component;

@Component
public class FuelTypeMapper {

    // @todo: no examples found in response?
    public FuelType map(String fuelType) {
        if (fuelType == null) {
            return null;
        }

        throw new UnsupportedOperationException("Not implemented yet, no fuel types found in traffic sign response");
    }
}
