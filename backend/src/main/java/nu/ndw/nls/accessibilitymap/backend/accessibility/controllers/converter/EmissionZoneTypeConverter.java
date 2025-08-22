package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.converter;

import jakarta.annotation.Nonnull;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionZoneTypeJson;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EmissionZoneTypeConverter implements Converter<String, EmissionZoneTypeJson> {

    @Override
    public EmissionZoneTypeJson convert(@Nonnull String value) {

        return EmissionZoneTypeJson.fromValue(value);
    }
}
