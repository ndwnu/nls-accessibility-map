package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccessibilityProperties {

    Long id;
    Integer versionId;
    boolean accessible;

}
