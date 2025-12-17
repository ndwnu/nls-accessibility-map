package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nu.ndw.nls.springboot.test.component.driver.docker.dto.Environment;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class DockerConfiguration {

    @NotEmpty
    private String serviceName;

    @Valid
    private List<Environment> environmentVariables = List.of();
}
