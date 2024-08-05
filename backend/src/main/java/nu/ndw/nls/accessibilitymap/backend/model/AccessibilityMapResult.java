package nu.ndw.nls.accessibilitymap.backend.model;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;

public record AccessibilityMapResult(List<RoadSection> inaccessibleRoadSections, RoadSection requestedRoadSection) {
}
