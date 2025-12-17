package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class ServiceConfiguration {

    @NotEmpty
    private String host;

    @NotNull
    @Min(1)
    @Max(65535)
    private Integer port;
}
