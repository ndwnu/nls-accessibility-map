package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto;

import java.util.Collection;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSection;

@Builder
public record Accessibility(
        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions,
        Collection<RoadSection> mergedAccessibility
) {

}
