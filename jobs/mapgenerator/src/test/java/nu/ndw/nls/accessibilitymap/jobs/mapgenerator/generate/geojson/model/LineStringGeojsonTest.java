package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.Test;

class LineStringGeojsonTest {

    @Test
    void getType_ok() {
        LineStringGeojson lineStringGeojson = new LineStringGeojson(Collections.emptyList());
        assertEquals("LineString", lineStringGeojson.getType());
    }
}