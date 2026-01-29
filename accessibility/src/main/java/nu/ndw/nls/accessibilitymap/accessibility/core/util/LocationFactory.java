package nu.ndw.nls.accessibilitymap.accessibility.core.util;

import java.util.Objects;
import lombok.AllArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LocationFactory {

    private final GeometryFactoryWgs84 geometryFactoryWgs84;

    public Location mapCoordinate(Double latitude, Double longitude) {
        if(Objects.isNull(latitude) || Objects.isNull(longitude)) {
            return null;
        }
        Point point = geometryFactoryWgs84.createPoint(new Coordinate(longitude, latitude));
        return new Location(point.getY(), point.getX(), point);
    }
}
