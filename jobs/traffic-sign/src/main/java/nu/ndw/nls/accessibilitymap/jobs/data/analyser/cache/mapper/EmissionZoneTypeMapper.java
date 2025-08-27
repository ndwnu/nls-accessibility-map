package nu.ndw.nls.accessibilitymap.jobs.data.analyser.cache.mapper;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import org.springframework.stereotype.Component;

@Component
public class EmissionZoneTypeMapper {

    public EmissionZoneType map(nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.EmissionZoneType emissionZoneType) {

        if (Objects.isNull(emissionZoneType)) {
            return EmissionZoneType.UNKNOWN;
        }

        return switch (emissionZoneType) {
            case ZERO_EMISSION_ZONE -> EmissionZoneType.ZERO;
            case LOW_EMISSION_ZONE -> EmissionZoneType.LOW;
            default -> EmissionZoneType.UNKNOWN;
        };
    }


}
