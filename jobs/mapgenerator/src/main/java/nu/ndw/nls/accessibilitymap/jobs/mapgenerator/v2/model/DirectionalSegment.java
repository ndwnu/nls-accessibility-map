package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.LineString;

@Getter
@Setter
@AllArgsConstructor
@Builder
public final class DirectionalSegment {

    private final Direction direction;

    private final LineString lineString;

    private List<TrafficSign> trafficSigns;

    private final Boolean accessible;
}
