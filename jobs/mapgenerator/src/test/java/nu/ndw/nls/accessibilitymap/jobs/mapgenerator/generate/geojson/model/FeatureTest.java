package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FeatureTest {

    @Test
    void getType_ok() {
        AccessibilityGeoJsonFeature accessibilityGeoJsonFeature = new AccessibilityGeoJsonFeature(1L, null, null);
        assertEquals("Feature", accessibilityGeoJsonFeature.getType());
    }
}