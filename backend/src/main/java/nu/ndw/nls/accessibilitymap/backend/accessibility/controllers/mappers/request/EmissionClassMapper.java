package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.request;

import com.google.common.base.Enums;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import org.springframework.stereotype.Component;

@Component
public class EmissionClassMapper {

    public Set<EmissionClass> mapEmissionClass(EmissionClassJson emissionClassJson) {
        if (Objects.isNull(emissionClassJson)) {
            return null;
        } else {
            return Enums.getIfPresent(EmissionClass.class, emissionClassJson.name())
                    .toJavaUtil()
                    .map(Set::of)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid emission classification type: %s"
                            .formatted(emissionClassJson.name())));
        }
    }
}
