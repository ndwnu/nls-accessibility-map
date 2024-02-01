package nu.ndw.nls.accessibilitymap.backend.model;

import lombok.Builder;
import org.locationtech.jts.geom.Point;

@Builder
public record AccessibilityRequest(Point startPoint, int municipalityId, double searchDistanceInMetres,
                                   VehicleProperties vehicleProperties) {


}
