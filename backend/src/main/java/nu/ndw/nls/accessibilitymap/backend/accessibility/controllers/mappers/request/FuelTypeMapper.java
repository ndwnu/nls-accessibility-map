package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.request;

import com.google.common.base.Enums;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.backend.exceptions.FuelTypeNotSupportedException;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import org.springframework.stereotype.Component;

@Component
public class FuelTypeMapper {

    public Set<FuelType> mapFuelType(FuelTypeJson fuelTypeJson) {
        if (Objects.isNull(fuelTypeJson)) {
            return null;
        } else {
            return Enums.getIfPresent(FuelType.class, fuelTypeJson.name())
                    .toJavaUtil()
                    .map(Set::of)
                    .orElseThrow(() -> new FuelTypeNotSupportedException("Invalid fuel type: %s".formatted(fuelTypeJson.name())));
        }

    }
}
