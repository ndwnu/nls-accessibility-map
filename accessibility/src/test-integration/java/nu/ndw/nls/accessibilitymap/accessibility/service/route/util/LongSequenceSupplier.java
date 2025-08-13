package nu.ndw.nls.accessibilitymap.accessibility.service.route.util;

public class LongSequenceSupplier {

    private long i;

    public LongSequenceSupplier() {
        this(1);
    }

    public LongSequenceSupplier(long start) {
        this.i = start;
    }

    public long next() {
        return i++;
    }
}
