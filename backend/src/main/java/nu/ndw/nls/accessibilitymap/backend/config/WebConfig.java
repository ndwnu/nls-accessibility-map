package nu.ndw.nls.accessibilitymap.backend.config;

import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /*
     * Use of marker interfaces fixes runtime errors when using Spring converters as lambda expressions.
     * See https://stackoverflow.com/questions/25711858
     */
    private interface VehicleTypeJsonConverter extends Converter<String, VehicleTypeJson> {

    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter((VehicleTypeJsonConverter) VehicleTypeJson::fromValue);
    }
}
