package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto;

import java.util.Collection;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;

@Builder
public record Accessibility(
        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions,
        Collection<RoadSection> combinedAccessibility) {

}
