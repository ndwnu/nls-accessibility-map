package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.Test;

class AccessibilityGeoJsonFeatureCollectionTest {

    @Test
    void getType_ok() {
        AccessibilityGeoJsonFeatureCollection accessibilityGeoJsonFeatureCollection
                = new AccessibilityGeoJsonFeatureCollection(Collections.emptyList());
        assertEquals("FeatureCollection", accessibilityGeoJsonFeatureCollection.getType());
    }
}