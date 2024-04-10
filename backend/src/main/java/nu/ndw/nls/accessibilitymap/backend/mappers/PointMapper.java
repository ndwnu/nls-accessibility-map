package nu.ndw.nls.accessibilitymap.backend.mappers;


import java.util.Objects;
import lombok.AllArgsConstructor;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PointMapper {
    private final GeometryFactoryWgs84 geometryFactoryWgs84;
    public Point mapCoordinateAllowNulls(Double latitude, Double longitude) {
        if (Objects.isNull(latitude) || Objects.isNull(longitude)) {
            return null;
        }

        return geometryFactoryWgs84.createPoint(new Coordinate(longitude, latitude));
    }
}
