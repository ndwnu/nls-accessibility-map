package nu.ndw.nls.accessibilitymap.test.performance.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "nu.ndw.nls.accessibilitymap.test.performance")
@Getter
@Setter
@Validated
public class Simulations {

    @NotNull @Valid
    private List<Simulation> simulations;
}
