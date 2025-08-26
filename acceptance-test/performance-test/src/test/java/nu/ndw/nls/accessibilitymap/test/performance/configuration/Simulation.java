package nu.ndw.nls.accessibilitymap.test.performance.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
public record Simulation(
        @NotNull String name,
        @DefaultValue("true") boolean active,
        @NotNull Configuration configuration,
        @NotNull Assertions assertions) {

}
