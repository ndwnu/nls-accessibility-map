package nu.ndw.nls.accessibilitymap.backend.mappers;


import static org.junit.jupiter.api.Assertions.*;

import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class PointMapperTest {

    private final PointMapper pointMapper = new PointMapper(new GeometryFactoryWgs84());

    @Test
    void mapCoordinateAllowNulls_ok_maps() {
        assertEquals(new GeometryFactoryWgs84().createPoint(new Coordinate(2.0, 1.0)),
                pointMapper.mapCoordinateAllowNulls(1.0, 2.0));
    }

    @Test
    void mapCoordinateAllowNulls_ok_nullsReturnNull() {
        assertNull(pointMapper.mapCoordinateAllowNulls(null, null));
        assertNull(pointMapper.mapCoordinateAllowNulls(1.0, null));
        assertNull(pointMapper.mapCoordinateAllowNulls(null, 1.0));
    }
}