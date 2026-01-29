package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import lombok.Builder;
import org.locationtech.jts.geom.Point;

@Builder
public record Location(double latitude, double longitude, Point point) {

}
