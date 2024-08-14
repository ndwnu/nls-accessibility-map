package nu.ndw.nls.accessibilitymap.backend.municipality.model;

import java.net.URL;
import java.time.LocalDate;
import lombok.Value;
import nu.ndw.nls.accessibilitymap.accessibility.model.MunicipalityBoundingBox;
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
    LocalDate dateLastCheck;
}
