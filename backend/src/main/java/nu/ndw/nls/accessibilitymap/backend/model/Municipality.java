package nu.ndw.nls.accessibilitymap.backend.model;

import java.net.URL;
import lombok.Value;
import org.locationtech.jts.geom.Point;

@Value
public class Municipality {
    Point startPoint;
    double searchDistanceInMetres;
    int municipalityId;
    String name;
    URL requestExemptionUrl;
    MunicipalityBoundingBox bounds;
}
