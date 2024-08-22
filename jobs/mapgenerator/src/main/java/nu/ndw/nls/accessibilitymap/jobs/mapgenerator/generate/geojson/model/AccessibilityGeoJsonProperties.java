package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccessibilityGeoJsonProperties {

    long id;
    int versionId;
    boolean accessible;

    TrafficSignType trafficSignType;
    String windowTimes;

}
