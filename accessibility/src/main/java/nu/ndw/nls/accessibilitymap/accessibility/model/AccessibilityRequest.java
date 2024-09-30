package nu.ndw.nls.accessibilitymap.accessibility.model;

import lombok.Builder;
import lombok.With;
import org.locationtech.jts.geom.Point;

@Builder
@With
public record AccessibilityRequest(
        Point startPoint,
        Integer municipalityId,
        double searchDistanceInMetres,
        VehicleProperties vehicleProperties) {

}
