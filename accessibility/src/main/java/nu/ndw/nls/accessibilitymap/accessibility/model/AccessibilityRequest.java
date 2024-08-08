package nu.ndw.nls.accessibilitymap.accessibility.model;

import lombok.Builder;
import org.locationtech.jts.geom.Point;

@Builder
public record AccessibilityRequest(Point startPoint, Integer municipalityId, double searchDistanceInMetres,
                                   VehicleProperties vehicleProperties) {


}
