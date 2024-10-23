package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.utils;

public class LongSequenceSupplier {

    private long i = 1;

    public long next() {
        return i++;
    }
}