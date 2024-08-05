package nu.ndw.nls.accessibilitymap.accessibility.model;

import java.net.URL;
import lombok.Value;
import org.locationtech.jts.geom.Point;

@Value
public class Municipality {
    Point startPoint;
    double searchDistanceInMetres;
    String municipalityId;
    int municipalityIdInteger;
    String name;
    URL requestExemptionUrl;
    MunicipalityBoundingBox bounds;
}
