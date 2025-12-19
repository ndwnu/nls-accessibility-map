package nu.ndw.nls.accessibilitymap.test.performance;

import java.util.List;
import nu.ndw.nls.springboot.test.component.state.ScenarioStateManagerConfiguration;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import nu.ndw.nls.springboot.test.component.state.StateManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public StateManager stateManager(
            List<StateManagement> stateManagementServices,
            ScenarioStateManagerConfiguration scenarioStateManagerConfiguration) {

        return new StateManager(stateManagementServices, scenarioStateManagerConfiguration);
    }
}
