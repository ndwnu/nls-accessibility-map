package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PointMapper {

    private final GeometryFactoryWgs84 geometryFactoryWgs84;

    public Optional<Point> mapCoordinate(Double latitude, Double longitude) {
        if (Objects.isNull(latitude) || Objects.isNull(longitude)) {
            return Optional.empty();
        }

        return Optional.of(geometryFactoryWgs84.createPoint(new Coordinate(longitude, latitude)));
    }
}
