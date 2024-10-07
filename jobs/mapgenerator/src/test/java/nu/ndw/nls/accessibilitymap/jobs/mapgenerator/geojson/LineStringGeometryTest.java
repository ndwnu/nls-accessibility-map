package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.LineStringGeometry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LineStringGeometryTest {

    @Test
    void getType_ok() {

        LineStringGeometry lineStringGeometry = LineStringGeometry.builder().build();

        assertThat("LineString").isEqualTo(lineStringGeometry.getType());
    }
}