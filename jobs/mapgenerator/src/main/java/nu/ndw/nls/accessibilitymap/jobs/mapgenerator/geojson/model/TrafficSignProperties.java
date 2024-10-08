package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model;

import java.net.URI;
import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.trafficsign.TrafficSignType;

@Getter
@Builder
public class TrafficSignProperties implements Properties {

    private long nwbRoadSectionId;

    private boolean accessible;

    private Direction direction;

    private TrafficSignType trafficSignType;

    private String windowTimes;

    private URI iconUrl;

    private boolean isTrafficSign;
}
