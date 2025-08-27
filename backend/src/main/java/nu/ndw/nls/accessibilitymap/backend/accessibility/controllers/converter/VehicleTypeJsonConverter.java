package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.converter;

import jakarta.annotation.Nonnull;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class VehicleTypeJsonConverter implements Converter<String, VehicleTypeJson> {

    @Override
    public VehicleTypeJson convert(@Nonnull String value) {

        return VehicleTypeJson.fromValue(value);
    }
}
