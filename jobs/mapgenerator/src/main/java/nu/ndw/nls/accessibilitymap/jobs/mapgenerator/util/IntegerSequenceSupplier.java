package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.util;

public class IntegerSequenceSupplier {

    private int i = 1;

    public int next() {
        return i++;
    }
}
