package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.model.RoadSectionFragment;

@With
@Getter
@Builder
public class EffectiveAccessibility {

    DirectionalRoadSectionAndTrafficSignGroupedById directionalRoadSectionAndTrafficSignGroupedById;

    List<RoadSectionFragment<EffectiveAccessibleDirectionalRoadSections>> roadSectionFragments;


}
