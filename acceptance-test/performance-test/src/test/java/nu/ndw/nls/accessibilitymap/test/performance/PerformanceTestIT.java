package nu.ndw.nls.accessibilitymap.test.performance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import nu.ndw.nls.accessibilitymap.test.performance.configuration.ActiveSimulationConfiguration;
import nu.ndw.nls.accessibilitymap.test.performance.configuration.Simulation;
import nu.ndw.nls.accessibilitymap.test.performance.configuration.Simulations;
import nu.ndw.nls.accessibilitymap.test.performance.gatling.GatlingRunner;
import nu.ndw.nls.accessibilitymap.test.performance.gatling.wrapper.SpringGatlingSimulationWrapper;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {Application.class})
class PerformanceTestIT {

    @Autowired
    private Simulations simulations;

    @TestFactory
    Collection<DynamicTest> runPerformanceTest() {

        return simulations.getSimulations().stream()
                .filter(Simulation::active)
                .map(simulation -> DynamicTest.dynamicTest(simulation.name(), () -> runSimulation(simulation)))
                .toList();
    }

    private void runSimulation(Simulation simulation) {

        ActiveSimulationConfiguration.INSTANCE.setSimulation(simulation);
        assertThat(GatlingRunner.runSimulation(SpringGatlingSimulationWrapper.class.getName())).isZero();
    }
}
