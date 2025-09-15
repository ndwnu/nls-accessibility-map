package nu.ndw.nls.accessibilitymap.test.performance.simulation.roadsections;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;


@Validated
public record RoadSectionsSimulationConfiguration(
        @NotNull Integer numberOfGraphNodes,
        @NotNull Integer numberOfTrafficSigns) {

}