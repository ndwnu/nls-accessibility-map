package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import org.junit.jupiter.api.Test;

class FeatureCollectionTest {

    @Test
    void getType_ok() {
        AccessibilityGeoJsonFeatureCollection accessibilityGeoJsonFeatureCollection
                = new AccessibilityGeoJsonFeatureCollection(Collections.emptyList());
        assertEquals("FeatureCollection", accessibilityGeoJsonFeatureCollection.getType());
    }
}