package nu.ndw.nls.accessibilitymap.backend.municipality.controllers.dto;

import java.net.URL;
import java.time.LocalDate;
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

    LocalDate dateLastCheck;
}
