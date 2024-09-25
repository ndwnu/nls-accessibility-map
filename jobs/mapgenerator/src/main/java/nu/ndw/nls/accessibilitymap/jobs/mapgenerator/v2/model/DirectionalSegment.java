package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import org.locationtech.jts.geom.LineString;

public record DirectionalSegment(Direction direction, LineString lineString, TrafficSign trafficSign,
                                 boolean accessible) {

}
