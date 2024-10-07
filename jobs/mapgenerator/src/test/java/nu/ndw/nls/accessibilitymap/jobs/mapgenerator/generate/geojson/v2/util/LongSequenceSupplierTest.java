package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.v2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.util.LongSequenceSupplier;
import org.junit.jupiter.api.Test;

class LongSequenceSupplierTest {

    @Test
    void next_ok() {

        LongSequenceSupplier longSequenceSupplier = new LongSequenceSupplier();

        assertEquals(1L, longSequenceSupplier.next());
        assertEquals(2L, longSequenceSupplier.next());
        assertEquals(3L, longSequenceSupplier.next());
    }
}