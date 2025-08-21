package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper;

import com.google.common.base.Enums;
import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionZoneTypeJson;
import org.springframework.stereotype.Component;

@Component
public class EmissionZoneTypeMapper {

    @SuppressWarnings("java:S1168")
    public EmissionZoneType mapEmissionZoneType(EmissionZoneTypeJson emissionZoneTypeJson) {

        if (Objects.isNull(emissionZoneTypeJson)) {
            return null;
        } else {
            return Enums.getIfPresent(EmissionZoneType.class, emissionZoneTypeJson.name())
                    .toJavaUtil()
                    .orElseThrow(
                            () -> new IllegalArgumentException("Invalid emission zone type: %s".formatted(emissionZoneTypeJson.name())));
        }
    }
}
