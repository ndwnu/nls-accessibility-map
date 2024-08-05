package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class GeoJsonProperties {

    @NotBlank
    private String pathDatePattern;

    @NotBlank
    private String rvvCode;

}
