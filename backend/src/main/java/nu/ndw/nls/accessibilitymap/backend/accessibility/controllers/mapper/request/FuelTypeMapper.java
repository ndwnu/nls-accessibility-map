package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.request;

import com.google.common.base.Enums;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import org.springframework.stereotype.Component;

@Component
public class FuelTypeMapper {

    @SuppressWarnings("java:S1168")
    public Set<FuelType> mapFuelType(FuelTypeJson fuelTypeJson) {
        if (Objects.isNull(fuelTypeJson)) {
            return null;
        } else {
            return Enums.getIfPresent(FuelType.class, fuelTypeJson.name())
                    .toJavaUtil()
                    .map(Set::of)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid fuel type: %s".formatted(fuelTypeJson.name())));
        }
    }
}
