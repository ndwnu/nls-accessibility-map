package nu.ndw.nls.accessibilitymap.backend.model;

import static nu.ndw.nls.routingmapmatcher.constants.GlobalConstants.WGS84_GEOMETRY_FACTORY;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Value;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

@Value
public class Municipality {

    Point startPoint;
    double searchDistanceInMetres;
    String municipalityId;

    public Municipality(double startCoordinateLongitude, double startCoordinateLatitude, double searchDistanceInMetres,
            String municipalityId) {
        // Latitude is the Y axis, longitude is the X axis.
        this.startPoint = WGS84_GEOMETRY_FACTORY.createPoint(
                new Coordinate(startCoordinateLongitude, startCoordinateLatitude));
        this.searchDistanceInMetres = searchDistanceInMetres;
        this.municipalityId = municipalityId;
    }

    private static final Pattern PATTERN = Pattern.compile(".{2}0*(\\d+)$");

    public int municipalityIdAsInteger() {
        Matcher m = PATTERN.matcher(municipalityId);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            throw new IllegalStateException("Incorrect municipalityId " + municipalityId);
        }
    }
}
