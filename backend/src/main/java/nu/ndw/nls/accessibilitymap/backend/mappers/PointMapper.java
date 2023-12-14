package nu.ndw.nls.accessibilitymap.backend.mappers;

import static nu.ndw.nls.routingmapmatcher.constants.GlobalConstants.WGS84_GEOMETRY_FACTORY;

import java.util.Objects;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
public class PointMapper {
    public Point mapCoordinateAllowNulls(Double latitude, Double longitude) {
        if (Objects.isNull(latitude) || Objects.isNull(longitude)) {
            return null;
        }

        return WGS84_GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

}
