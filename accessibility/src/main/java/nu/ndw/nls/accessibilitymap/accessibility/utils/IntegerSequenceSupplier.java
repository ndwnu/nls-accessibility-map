package nu.ndw.nls.accessibilitymap.accessibility.utils;

public class IntegerSequenceSupplier {

    private int i;

    public IntegerSequenceSupplier() {
        this(1);
    }

    public IntegerSequenceSupplier(int start) {
        this.i = start;
    }

    public int next() {
        return i++;
    }
}
