package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DirectionalRoadSectionAndTrafficSignGroupedById {
    DirectionalRoadSectionAndTrafficSign forward;
    DirectionalRoadSectionAndTrafficSign backward;
}
