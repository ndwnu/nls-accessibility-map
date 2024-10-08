package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FeatureCollectionTest {

    @Test
    void getType() {

        assertThat(FeatureCollection.builder().build().getType()).isEqualTo("FeatureCollection");
    }
}