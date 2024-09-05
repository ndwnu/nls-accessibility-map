package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DirectionTest {

    @Test
    void isForward_ok() {
        assertTrue(Direction.FORWARD.isForward());
        assertFalse(Direction.BACKWARD.isForward());
    }
}