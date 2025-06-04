package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.request;

import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import org.springframework.stereotype.Component;

@Component
public class EmissionClassMapper {

    @SuppressWarnings("java:S1168")
    public Set<EmissionClass> mapEmissionClass(EmissionClassJson emissionClassJson) {
        if (Objects.isNull(emissionClassJson)) {
            return null;
        } else {
            return switch (emissionClassJson) {
                // Should not have any impact because there is no official zero emission class in the official definitions.
                // Zero can never be used in the emission zone data set we get from W&R, therefore, we should not use it.
                // See also EmissionService::isValid in the job/traffic-sign module for a more detailed explanation.
                case ZERO -> Set.of();
                case EURO_1 -> Set.of(EmissionClass.EURO_1);
                case EURO_2 -> Set.of(EmissionClass.EURO_2);
                case EURO_3 -> Set.of(EmissionClass.EURO_3);
                case EURO_4 -> Set.of(EmissionClass.EURO_4);
                case EURO_5 -> Set.of(EmissionClass.EURO_5);
                case EURO_6 -> Set.of(EmissionClass.EURO_6);
            };
        }
    }
}
