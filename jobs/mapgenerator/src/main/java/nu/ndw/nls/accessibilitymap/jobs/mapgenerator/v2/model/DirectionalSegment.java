package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import lombok.Builder;
import org.locationtech.jts.geom.LineString;
@Builder
public record DirectionalSegment(Direction direction, LineString lineString, TrafficSign trafficSign,
                                 Boolean accessible) {

}
