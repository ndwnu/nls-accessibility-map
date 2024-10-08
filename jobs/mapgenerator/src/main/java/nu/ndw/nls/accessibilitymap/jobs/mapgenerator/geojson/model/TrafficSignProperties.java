package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model;

import java.net.URI;
import lombok.Builder;
import lombok.Value;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.trafficsign.TrafficSignType;

@Value
@Builder
public class TrafficSignProperties implements Properties {

    long nwbRoadSectionId;

    boolean accessible;

    Direction direction;

    TrafficSignType trafficSignType;

    String windowTimes;

    URI iconUrl;

    boolean isTrafficSign;
}
