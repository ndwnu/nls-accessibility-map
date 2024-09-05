package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

@ToString
@Getter
@With
@Builder
@EqualsAndHashCode
public class EffectiveAccessibleDirectionalRoadSections {

    Boolean accessibility;
    DirectionalRoadSectionAndTrafficSign backward;
    DirectionalRoadSectionAndTrafficSign forward;
}
