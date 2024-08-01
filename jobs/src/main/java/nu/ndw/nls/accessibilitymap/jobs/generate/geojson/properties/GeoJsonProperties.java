package nu.ndw.nls.accessibilitymap.jobs.generate.geojson.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class GeoJsonProperties {

    @NotBlank
    private String name;

}
