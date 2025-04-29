package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.request;

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
                case _1 -> Set.of(EmissionClass.EURO_1);
                case _2 -> Set.of(EmissionClass.EURO_2);
                case _3 -> Set.of(EmissionClass.EURO_3);
                case _4 -> Set.of(EmissionClass.EURO_4);
                case _5 -> Set.of(EmissionClass.EURO_5);
                case _6 -> Set.of(EmissionClass.EURO_6);
            };
        }
    }
}
