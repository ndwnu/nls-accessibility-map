package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.request;

import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.EmissionClassJson;
import org.springframework.stereotype.Component;

@Component
public class EmissionClassMapperV2 {

    @SuppressWarnings("java:S1168")
    public Set<EmissionClass> map(EmissionClassJson emissionClass) {
        if (Objects.isNull(emissionClass)) {
            return null;
        } else {
            return switch (emissionClass) {
                // Should not have any impact because there is no official zero emission class in the official definitions.
                // Zero can never be used in the emission zone data set we get from W&R, therefore, we should not use it.
                // See also EmissionService::isValid in the job/traffic-sign module for a more detailed explanation.
                case EmissionClassJson.ZERO -> Set.of();
                case EmissionClassJson.EURO_1 -> Set.of(EmissionClass.EURO_1);
                case EmissionClassJson.EURO_2 -> Set.of(EmissionClass.EURO_2);
                case EmissionClassJson.EURO_3 -> Set.of(EmissionClass.EURO_3);
                case EmissionClassJson.EURO_4 -> Set.of(EmissionClass.EURO_4);
                case EmissionClassJson.EURO_5 -> Set.of(EmissionClass.EURO_5);
                case EmissionClassJson.EURO_6 -> Set.of(EmissionClass.EURO_6);
            };
        }
    }
}
