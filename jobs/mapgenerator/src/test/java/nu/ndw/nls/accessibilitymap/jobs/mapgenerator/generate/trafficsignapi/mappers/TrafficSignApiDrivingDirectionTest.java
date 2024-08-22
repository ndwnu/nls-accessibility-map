package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.trafficsignapi.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers.TrafficSignApiDrivingDirection;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import org.junit.jupiter.api.Test;

class TrafficSignApiDrivingDirectionTest {

    public static final DirectionType DRIVING_DIRECTION_FORWARD = DirectionType.FORTH;
    public static final DirectionType DRIVING_DIRECTION_REVERSE = DirectionType.BACK;
    public static final DirectionType DRIVING_DIRECTION_BOTH_DIRECTIONS = DirectionType.BOTH;

    @Test
    void from_ok() {
        assertEquals(TrafficSignApiDrivingDirection.FORWARD, TrafficSignApiDrivingDirection.from(DRIVING_DIRECTION_FORWARD));
        assertEquals(TrafficSignApiDrivingDirection.REVERSE, TrafficSignApiDrivingDirection.from(DRIVING_DIRECTION_REVERSE));
        assertEquals(TrafficSignApiDrivingDirection.BOTH, TrafficSignApiDrivingDirection.from(DRIVING_DIRECTION_BOTH_DIRECTIONS));
        assertEquals(TrafficSignApiDrivingDirection.UNKNOWN, TrafficSignApiDrivingDirection.from(null));
    }

    @Test
    void isForward_ok() {
        assertTrue(TrafficSignApiDrivingDirection.FORWARD.isForward());
        assertTrue(TrafficSignApiDrivingDirection.BOTH.isForward());
        assertTrue(TrafficSignApiDrivingDirection.UNKNOWN.isForward());
        assertFalse(TrafficSignApiDrivingDirection.REVERSE.isForward());
    }

    @Test
    void isReverse_ok() {
        assertTrue(TrafficSignApiDrivingDirection.REVERSE.isReverse());
        assertTrue(TrafficSignApiDrivingDirection.BOTH.isReverse());
        assertTrue(TrafficSignApiDrivingDirection.UNKNOWN.isReverse());
        assertFalse(TrafficSignApiDrivingDirection.FORWARD.isReverse());
    }

    @Test
    void getValue_ok() {
        assertEquals(DRIVING_DIRECTION_FORWARD, TrafficSignApiDrivingDirection.FORWARD.getValue());
        assertEquals(DRIVING_DIRECTION_REVERSE, TrafficSignApiDrivingDirection.REVERSE.getValue());
        assertEquals(DRIVING_DIRECTION_BOTH_DIRECTIONS, TrafficSignApiDrivingDirection.BOTH.getValue());
        assertNull(TrafficSignApiDrivingDirection.UNKNOWN.getValue());
    }

}