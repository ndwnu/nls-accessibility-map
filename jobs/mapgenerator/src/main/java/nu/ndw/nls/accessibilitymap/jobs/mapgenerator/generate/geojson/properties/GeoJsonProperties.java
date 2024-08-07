package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import nu.ndw.nls.events.NlsEventSubjectType;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class GeoJsonProperties {

    @NotBlank
    private String pathDatePattern;

    @NotBlank
    private String rvvCode;

    @NotNull
    private NlsEventSubjectType publisherEventSubjectType;
}
