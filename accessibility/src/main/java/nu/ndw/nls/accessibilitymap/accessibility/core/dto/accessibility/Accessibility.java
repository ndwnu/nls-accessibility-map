package nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasonGroup;

@Builder
public record Accessibility(
        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions,
        Collection<RoadSection> combinedAccessibility,
        Collection<RoadSection> unroutableRoadSections,
        Optional<DirectionalSegment> toDirectionalSegment,
        List<AccessibilityReasonGroup> reasons) {

}
