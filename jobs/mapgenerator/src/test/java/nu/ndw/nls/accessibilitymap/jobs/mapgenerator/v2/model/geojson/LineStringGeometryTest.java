package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.geojson;

import static org.assertj.core.api.Assertions.assertThat;

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