package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import nu.ndw.nls.events.NlsEventSubjectType;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class TrafficSignConfiguration {

    @NotBlank
    private String pathDatePattern;

    @NotNull
    private NlsEventSubjectType publisherEventSubjectType;
}
