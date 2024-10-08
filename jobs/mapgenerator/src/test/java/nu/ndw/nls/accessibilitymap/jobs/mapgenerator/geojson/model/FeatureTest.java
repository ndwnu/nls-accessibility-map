package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FeatureTest {

    @Test
    void getType() {

        assertThat(Feature.builder().build().getType()).isEqualTo("Feature");
    }
}