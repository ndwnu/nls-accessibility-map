package nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.request;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.EmissionZoneTypeJson;
import org.springframework.stereotype.Component;

@Component
public class EmissionZoneTypeMapper {

    public EmissionZoneType map(EmissionZoneTypeJson emissionZoneTypeJson) {

        if (Objects.isNull(emissionZoneTypeJson)) {
            return null;
        } else {
            return switch (emissionZoneTypeJson) {
                case LOW_EMISSION_ZONE -> EmissionZoneType.LOW;
                case ZERO_EMISSION_ZONE -> EmissionZoneType.ZERO;
            };
        }
    }
}
