package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.util;

public class LongSequenceSupplier {

    private long i = 1;

    public long next() {
        return i++;
    }
}
