package nu.ndw.nls.accessibilitymap.accessibility.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

class LocationFactoryTest {

    private final LocationFactory locationFactory = new LocationFactory(new GeometryFactoryWgs84());

    @Test
    void mapCoordinate() {
        Location result = locationFactory.mapCoordinate(1.0, 2.0);

        assertThat(result.point()).isEqualTo(newPoint(2.0, 1.0));
        assertThat(result.latitude()).isEqualTo(1.0);
        assertThat(result.longitude()).isEqualTo(2.0);
    }

    private static Point newPoint(double x, double y) {
        return new GeometryFactoryWgs84().createPoint(new Coordinate(x, y));
    }
}
