package nu.ndw.nls.accessibilitymap.backend.mappers;

import static nu.ndw.nls.routingmapmatcher.constants.GlobalConstants.WGS84_GEOMETRY_FACTORY;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class PointMapperTest {

    private final PointMapper pointMapper = new PointMapper();

    @Test
    void mapCoordinateAllowNulls_ok_maps() {
        assertEquals(WGS84_GEOMETRY_FACTORY.createPoint(new Coordinate(2.0, 1.0)),
                pointMapper.mapCoordinateAllowNulls(1.0, 2.0));
    }

    @Test
    void mapCoordinateAllowNulls_ok_nullsReturnNull() {
        assertNull(pointMapper.mapCoordinateAllowNulls(null, null));
        assertNull(pointMapper.mapCoordinateAllowNulls(1.0, null));
        assertNull(pointMapper.mapCoordinateAllowNulls(null, 1.0));
    }
}