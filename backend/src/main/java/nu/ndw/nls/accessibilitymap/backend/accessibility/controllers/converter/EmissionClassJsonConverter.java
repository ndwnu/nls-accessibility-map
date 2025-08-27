package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.converter;

import jakarta.annotation.Nonnull;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EmissionClassJsonConverter implements Converter<String, EmissionClassJson> {

    @Override
    public EmissionClassJson convert(@Nonnull String value) {

        return EmissionClassJson.fromValue(value);
    }
}
