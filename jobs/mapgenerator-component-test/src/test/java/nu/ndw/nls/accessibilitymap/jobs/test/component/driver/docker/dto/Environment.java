package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.validation.annotation.Validated;

@Builder
@Validated
public record Environment(
        @NotNull String key,
        @NotNull String value) {

}
