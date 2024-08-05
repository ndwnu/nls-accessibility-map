package nu.ndw.nls.accessibilitymap.accessibility.municipality;

import java.net.URL;
import lombok.Value;
import nu.ndw.nls.accessibilitymap.accessibility.model.MunicipalityBoundingBox;

@Value
public class MunicipalityProperty {

    String name;
    double startCoordinateLongitude;
    double startCoordinateLatitude;
    double searchDistanceInMetres;
    String municipalityId;
    URL requestExemptionUrl;
    MunicipalityBoundingBox bounds;
}
