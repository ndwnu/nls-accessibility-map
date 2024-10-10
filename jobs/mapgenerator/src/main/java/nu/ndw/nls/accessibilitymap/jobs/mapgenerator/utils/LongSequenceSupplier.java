package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.utils;

public class LongSequenceSupplier {

    private long i = 1;

    public long next() {
        return i++;
    }
}
