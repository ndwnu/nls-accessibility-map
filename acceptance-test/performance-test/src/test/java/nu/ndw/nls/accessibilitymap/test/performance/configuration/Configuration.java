package nu.ndw.nls.accessibilitymap.test.performance.configuration;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.test.performance.simulation.AbstractSimulation;
import org.springframework.validation.annotation.Validated;

@Validated
public record Configuration(
        @NotNull Class<AbstractSimulation> simulationClass,
        @NotNull Duration rampUpTime,
        @NotNull Integer concurrentUsers,

        // An optional configuration set that can be added to your simulation
        // If you want to use this you need to override and implement AbstractSimulation::getSimulationSpecificConfigurationClass
        Map<String, Object> simulationSpecificConfiguration) {

}
