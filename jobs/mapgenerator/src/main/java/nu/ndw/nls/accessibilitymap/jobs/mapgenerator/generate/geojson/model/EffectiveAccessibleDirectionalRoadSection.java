package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

@With
@Getter
@Builder
@EqualsAndHashCode
@ToString
public class EffectiveAccessibleDirectionalRoadSection {

    Boolean accessibility;
    DirectionalRoadSectionAndTrafficSign roadSection;
}
