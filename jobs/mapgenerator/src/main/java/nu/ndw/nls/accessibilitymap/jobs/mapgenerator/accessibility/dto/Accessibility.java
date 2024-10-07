package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto;

import java.util.Collection;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model.RoadSection;

@Builder
public record Accessibility(
        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions,
        Collection<RoadSection> mergedAccessibility
) {

}
