package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.converter;

import jakarta.annotation.Nonnull;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FuelTypeJsonConverter implements Converter<String, FuelTypeJson> {

    @Override
    public FuelTypeJson convert(@Nonnull String value) {

        return FuelTypeJson.fromValue(value);
    }
}
