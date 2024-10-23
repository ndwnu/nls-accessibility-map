package nu.ndw.nls.accessibilitymap.jobs.test.component.core;

public interface StateManagement {

    void clearStateAfterEachScenario();

    default void clearStateAfterAllScenarios() {

    }

    default void prepareBeforeEachScenario() {

    }

    default void prepareBeforeAllScenarios() {

    }
}
