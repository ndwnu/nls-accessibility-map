
package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.dto;

import jakarta.validation.constraints.NotEmpty;
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
public class JobArgument {

    @NotEmpty
    private String parameter;

    private String value;
}
