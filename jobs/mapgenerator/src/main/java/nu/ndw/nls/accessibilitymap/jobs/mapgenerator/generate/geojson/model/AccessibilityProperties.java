package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccessibilityProperties {

    Long id;
    Integer versionId;
    LocalDate nwbValidFrom;
    LocalDate validFrom;
    boolean accessible;

}
