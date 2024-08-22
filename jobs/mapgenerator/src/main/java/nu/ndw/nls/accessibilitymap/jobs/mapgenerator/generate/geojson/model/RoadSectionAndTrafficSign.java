package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import lombok.Builder;
import lombok.Data;
import lombok.With;

@Data
@Builder
@With
public class RoadSectionAndTrafficSign<T, U> {

    private final T roadSection;
    private final U trafficSign;

}
