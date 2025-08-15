package nu.ndw.nls.accessibilitymap.accessibility.utils;

@FunctionalInterface
public interface FixtureConfigurator {

    void configure();

    default FixtureConfigurator compose(FixtureConfigurator before) {
        return () -> {
            before.configure();
            this.configure();
        };
    }
}
