package nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto;

import java.util.Collection;
import java.util.Optional;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;

@Builder
public record Accessibility(
        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions,
        Collection<RoadSection> combinedAccessibility) {

   public Optional<RoadSection> findById(long id) {
        return combinedAccessibility.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }
}
