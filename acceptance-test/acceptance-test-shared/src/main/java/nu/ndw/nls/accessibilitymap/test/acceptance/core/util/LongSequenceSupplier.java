package nu.ndw.nls.accessibilitymap.test.acceptance.core.util;

public class LongSequenceSupplier {

    private long i = 1;

    public long next() {
        return i++;
    }
}