package nu.ndw.nls.accessibilitymap.accessibility.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

class LocationFactoryTest {

    private final LocationFactory locationFactory = new LocationFactory(new GeometryFactoryWgs84());

    @ParameterizedTest
    @CsvSource(value = {
            "1.0,   2.0",
            "null,  2.0",
            "1.0,   null",
            "null,  null",
    }, nullValues = "null")
    void mapCoordinate(Double latitude, Double longitude) {
        Location result = locationFactory.mapCoordinate(latitude, longitude);

        if (Objects.isNull(latitude) || Objects.isNull(longitude)) {
            assertThat(result).isNull();
        } else {
            assertThat(result.point()).isEqualTo(newPoint(longitude, 1.0));
            assertThat(result.latitude()).isEqualTo(latitude);
            assertThat(result.longitude()).isEqualTo(longitude);
        }
    }

    private static Point newPoint(double x, double y) {
        return new GeometryFactoryWgs84().createPoint(new Coordinate(x, y));
    }
}
