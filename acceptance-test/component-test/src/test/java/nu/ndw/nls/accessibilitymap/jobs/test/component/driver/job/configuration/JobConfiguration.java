
package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.dto.JobArgument;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class JobConfiguration {

    @NotEmpty
    private String command;

    private List<JobArgument> arguments = List.of();

    private boolean runAsService = true;

    /**
     * Should only be configured when runAsService is set to true.
     * TODO: Add custom validator for this
     */
    @Valid
    private ServiceConfiguration serviceConfiguration;

    /**
     * Should only be configured when runAsService is set to false.
     * TODO: Add custom validator for this
     */
    @Valid
    private DockerConfiguration dockerConfiguration;
}
