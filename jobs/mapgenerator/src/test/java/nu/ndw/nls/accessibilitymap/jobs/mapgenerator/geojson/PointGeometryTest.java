package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.PointGeometry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointGeometryTest {

    @Test
    void getType_ok() {

        PointGeometry pointGeometry = PointGeometry.builder().build();

        assertThat("Point").isEqualTo(pointGeometry.getType());
    }
}