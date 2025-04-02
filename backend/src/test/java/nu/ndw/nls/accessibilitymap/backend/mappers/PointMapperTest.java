package nu.ndw.nls.accessibilitymap.backend.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.stream.Stream;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

class PointMapperTest {

    private final PointMapper pointMapper = new PointMapper(new GeometryFactoryWgs84());

    @Test
    void mapCoordinate_maps() {
        Optional<Point> result = pointMapper.mapCoordinate(1.0, 2.0);
        assertThat(result).contains(newPoint(2.0, 1.0));
    }

    private static Point newPoint(double x, double y) {
        return new GeometryFactoryWgs84().createPoint(new Coordinate(x, y));
    }

    @ParameterizedTest
    @MethodSource("mapCoordinate")
    void mapCoordinate_empty(Double latitude, Double longitude) {
        Optional<Point> result = pointMapper.mapCoordinate(latitude, longitude);
        assertThat(result).isEmpty();
    }

    static Stream<Arguments> mapCoordinate() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(1.0, null),
                Arguments.of(null, 1.0)
        );
    }
}
