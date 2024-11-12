package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PointGeometryTest {

    @Test
    void getType() {

        assertThat(PointGeometry.builder().build().getType()).isEqualTo("Point");
    }
}
