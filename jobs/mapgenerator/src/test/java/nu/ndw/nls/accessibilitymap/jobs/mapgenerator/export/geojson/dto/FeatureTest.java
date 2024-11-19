package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FeatureTest {

    @Test
    void getType_ok() {

        assertThat(Feature.builder().build().getType()).isEqualTo("Feature");
    }
}
