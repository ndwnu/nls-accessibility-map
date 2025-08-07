package nu.ndw.nls.accessibilitymap.accessibility.service.dto;

import java.util.Collection;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;

@Builder
public record Accessibility(
        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions,
        Collection<RoadSection> combinedAccessibility) {

    public boolean matchedRoadSectionIsAccessible(int roadSectionId) {
        return combinedAccessibility.stream()
                .filter(rs -> rs.getId() == roadSectionId)
                .findFirst()
                .map(roadSection -> roadSection.isBackwardAccessible() &&
                        roadSection.isForwardAccessible())
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "The road section with id " + roadSectionId + " does not exist."));

    }
}
