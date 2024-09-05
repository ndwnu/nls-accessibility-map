package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.suppliers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GeoJsonIdSequenceSupplierTest {

    @Test
    void next_ok() {
        GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier = new GeoJsonIdSequenceSupplier();
        assertEquals(1, geoJsonIdSequenceSupplier.next());
        assertEquals(2, geoJsonIdSequenceSupplier.next());
        assertEquals(3, geoJsonIdSequenceSupplier.next());
    }
}