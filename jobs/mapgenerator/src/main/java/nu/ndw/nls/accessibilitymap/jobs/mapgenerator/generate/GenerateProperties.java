package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GenerateGeoJsonType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix ="nu.ndw.nls.accessibilitymap.jobs.generate")
public class GenerateProperties {

    @NotNull
    private Map<GenerateGeoJsonType, GeoJsonProperties> geojson;

}