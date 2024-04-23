package nu.ndw.nls.accessibilitymap.backend.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Value;
import org.locationtech.jts.geom.Point;

@Value
public class Municipality {

    private static final Pattern PATTERN = Pattern.compile(".{2}0*(\\d+)$");
    Point startPoint;
    double searchDistanceInMetres;
    String municipalityId;
    String name;
    String requestExemptionUrl;
    MunicipalityBoundingBox bounds;

    public int getMunicipalityIdAsInteger() {
        Matcher m = PATTERN.matcher(municipalityId);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            throw new IllegalStateException("Incorrect municipalityId " + municipalityId);
        }
    }
}
