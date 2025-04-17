package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.request;

import com.google.common.base.Enums;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClassification;
import nu.ndw.nls.accessibilitymap.backend.exceptions.EmissionClassNotSupportedException;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import org.springframework.stereotype.Component;

@Component
public class EmissionClassificationMapper {

    public Set<EmissionClassification> mapEmissionClassification(EmissionClassJson emissionClassJson) {
        if (Objects.isNull(emissionClassJson)) {
            return null;
        } else {
            return Enums.getIfPresent(EmissionClassification.class, emissionClassJson.name())
                    .toJavaUtil()
                    .map(Set::of)
                    .orElseThrow(() -> new EmissionClassNotSupportedException("Invalid emission classification type: %s"
                            .formatted(emissionClassJson.name())));
        }

    }
}
