package nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;

@Builder
public record Accessibility(
        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions,
        Collection<RoadSection> combinedAccessibility,
        Collection<RoadSection> unroutableRoadSections,
        Optional<RoadSection> toRoadSection,
        /*
         * A list of unique reason collection groups.
         */
        List<List<AccessibilityReason>> reasons) {

}
