package nu.ndw.nls.accessibilitymap.backend.model;

import java.util.List;

public record AccessibilityMapResult(List<RoadSection> inaccessibleRoadSections, RoadSection requestedRoadSection) {
}
