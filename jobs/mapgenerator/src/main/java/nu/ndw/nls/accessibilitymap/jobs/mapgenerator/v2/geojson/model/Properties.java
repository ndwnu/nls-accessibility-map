package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model;

import java.net.URI;
import lombok.Builder;
import lombok.Value;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;

@Value
@Builder
public class Properties {

    long id;

    int versionId;

    boolean accessible;

    Direction direction;

    TrafficSignType trafficSignType;

    String windowTimes;

    URI iconUrl;
}
