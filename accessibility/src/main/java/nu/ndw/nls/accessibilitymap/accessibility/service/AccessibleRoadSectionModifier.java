package nu.ndw.nls.accessibilitymap.accessibility.service;

import java.util.Collection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;

@FunctionalInterface
@SuppressWarnings("java:S1711") // This is because we want to have a proper name signature for each variable because they are the same type.
public interface AccessibleRoadSectionModifier {

    void modify(
            Collection<RoadSection> roadsSectionsWithoutAppliedRestrictions,
            Collection<RoadSection> roadSectionsWithAppliedRestrictions);
}
