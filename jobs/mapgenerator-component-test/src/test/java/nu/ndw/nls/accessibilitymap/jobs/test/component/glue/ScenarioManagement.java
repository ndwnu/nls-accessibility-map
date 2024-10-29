package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;

@RequiredArgsConstructor
public class ScenarioManagement {

    private final List<StateManagement> stateManagedServices;

    @Before
    public void beforeScenario() {

        resetAllState();
        stateManagedServices.forEach(StateManagement::prepareBeforeEachScenario);
    }

    @After
    public void afterScenario() {

        resetAllState();
    }

    private void resetAllState() {

        stateManagedServices.forEach(StateManagement::clearStateAfterEachScenario);
    }
}
