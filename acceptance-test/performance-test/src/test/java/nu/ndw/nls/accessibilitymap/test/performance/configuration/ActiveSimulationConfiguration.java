package nu.ndw.nls.accessibilitymap.test.performance.configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ActiveSimulationConfiguration {

    public static final ActiveSimulationConfiguration INSTANCE = new ActiveSimulationConfiguration();

    @Setter
    private Simulation simulation;

}
