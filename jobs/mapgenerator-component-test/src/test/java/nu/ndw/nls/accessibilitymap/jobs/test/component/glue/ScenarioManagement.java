package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;

@RequiredArgsConstructor
public class ScenarioManagement {

    private final List<StateManagement> stateManagedServices;

    @BeforeAll
    public static void beforeAllScenarios() {

//        stateManagedServices.forEach(StateManagement::prepareBeforeAllScenarios);
    }

    @Before
    public void beforeScenario() {

        resetAllState();
        stateManagedServices.forEach(StateManagement::prepareBeforeEachScenario);
    }

    @After
    public void afterScenario() {

        resetAllState();
    }

    @AfterAll
    public static void afterAllScenarios() {

//        stateManagedServices.forEach(StateManagement::clearStateAfterAllScenarios);
    }

    private void resetAllState() {

        stateManagedServices.forEach(StateManagement::clearStateAfterEachScenario);
    }
}
