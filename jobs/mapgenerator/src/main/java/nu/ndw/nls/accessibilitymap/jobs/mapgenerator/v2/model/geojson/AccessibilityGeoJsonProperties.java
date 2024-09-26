package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.geojson;

import java.net.URI;
import lombok.Builder;
import lombok.Value;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSignType;

@Value
@Builder
public class AccessibilityGeoJsonProperties {

    long id;

    int versionId;

    boolean accessible;

    Direction direction;

    TrafficSignType trafficSignType;

    String windowTimes;

    URI iconUrl;
}
