package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LineStringGeometryTest {

    @Test
    void getType() {

        assertThat(LineStringGeometry.builder().build().getType()).isEqualTo("LineString");
    }
}
