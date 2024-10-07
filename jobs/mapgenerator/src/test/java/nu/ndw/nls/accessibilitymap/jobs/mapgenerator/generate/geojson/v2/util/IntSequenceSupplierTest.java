package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.v2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.util.IntegerSequenceSupplier;
import org.junit.jupiter.api.Test;

class IntSequenceSupplierTest {

    @Test
    void next_ok() {

        IntegerSequenceSupplier integerSequenceSupplier = new IntegerSequenceSupplier();

        assertEquals(1, integerSequenceSupplier.next());
        assertEquals(2, integerSequenceSupplier.next());
        assertEquals(3, integerSequenceSupplier.next());
    }
}