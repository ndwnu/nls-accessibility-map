package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper;

import com.google.common.base.Enums;
import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.generated.model.v2.FuelTypeJson;
import org.springframework.stereotype.Component;

@Component
public class FuelTypeMapperV2 {

    @SuppressWarnings("java:S1168")
    public FuelType map(FuelTypeJson fuelTypeJson) {
        if (Objects.isNull(fuelTypeJson)) {
            return null;
        } else {
            return Enums.getIfPresent(FuelType.class, fuelTypeJson.name())
                    .toJavaUtil()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid fuel type: %s".formatted(fuelTypeJson.name())));
        }
    }

    public FuelTypeJson map(FuelType fuelType) {
        return Enums.getIfPresent(FuelTypeJson.class, fuelType.name())
                .toJavaUtil().orElseThrow(() ->
                        new IllegalArgumentException("Invalid fuel type: %s".formatted(fuelType.name())));
    }
}
