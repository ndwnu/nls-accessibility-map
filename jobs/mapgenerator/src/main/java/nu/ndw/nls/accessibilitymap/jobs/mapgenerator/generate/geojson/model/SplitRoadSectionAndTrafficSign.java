package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalTrafficSign;

@With
@Value
@Builder
public class SplitRoadSectionAndTrafficSign {

    DirectionalRoadSection roadSectionAccessible;
    DirectionalRoadSection roadSectionInaccessible;
    DirectionalTrafficSign trafficSign;

}
