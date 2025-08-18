package nu.ndw.nls.accessibilitymap.accessibility.service.dto;

import java.util.Collection;
import java.util.List;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;

@Builder
public record Accessibility(
        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions,
        Collection<RoadSection> combinedAccessibility,
        RoadSection toRoadSection,
        /*
         * A list of unique reason collection groups.
         */
        List<List<AccessibilityReason>> reasons) {

}
