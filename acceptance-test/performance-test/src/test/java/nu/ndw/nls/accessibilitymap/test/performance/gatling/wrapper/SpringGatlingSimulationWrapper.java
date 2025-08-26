package nu.ndw.nls.accessibilitymap.test.performance.gatling.wrapper;

import io.gatling.javaapi.core.Simulation;
import nu.ndw.nls.accessibilitymap.test.performance.ApplicationContextProvider;
import nu.ndw.nls.accessibilitymap.test.performance.configuration.ActiveSimulationConfiguration;
import nu.ndw.nls.accessibilitymap.test.performance.simulation.AbstractSimulation;

public class SpringGatlingSimulationWrapper extends Simulation {

    private final AbstractSimulation simulation;

    {
        simulation = ApplicationContextProvider
                .getBean(ActiveSimulationConfiguration.INSTANCE.getSimulation().configuration().simulationClass());
        simulation.before();

        setUp(simulation.getSimulations()).assertions(simulation.getAssertions());
    }

    @Override
    public void after() {

        super.after();
        simulation.after();
    }
}
