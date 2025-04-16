package nu.ndw.nls.accessibilitymap.backend.municipality;

import java.net.URL;
import java.time.LocalDate;
import nu.ndw.nls.accessibilitymap.backend.municipality.controllers.dto.MunicipalityBoundingBox;

public record MunicipalityProperty(String name, double startCoordinateLongitude, double startCoordinateLatitude,
                                   double searchDistanceInMetres, String municipalityId, URL requestExemptionUrl,
                                   MunicipalityBoundingBox bounds, LocalDate dateLastCheck) {

}
