package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.request;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.generated.model.v2.EmissionZoneTypeJson;
import org.springframework.stereotype.Component;

@Component
public class EmissionZoneTypeMapperV2 {

    public EmissionZoneType map(EmissionZoneTypeJson emissionZoneType) {

        if (Objects.isNull(emissionZoneType)) {
            return null;
        } else {
            return switch (emissionZoneType) {
                case LOW_EMISSION_ZONE -> EmissionZoneType.LOW;
                case ZERO_EMISSION_ZONE -> EmissionZoneType.ZERO;
            };
        }
    }
}
