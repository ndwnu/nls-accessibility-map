package nu.ndw.nls.accessibilitymap.accessibility.service.dto;

import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;

@Builder
public record AccessibilityContext(
        GraphHopperNetwork graphHopperNetwork,
        SortedMap<Long, AccessibilityNwbRoadSection> accessibilityNwbRoadSections,
        List<TrafficSign> trafficSigns,
        int nwbVersionId
) {

    public Optional<AccessibilityNwbRoadSection> findAllAccessibilityNwbRoadSectionById(long roadSectionId) {

        return Optional.ofNullable(accessibilityNwbRoadSections.get(roadSectionId));
    }

    public List<AccessibilityNwbRoadSection> findAllAccessibilityNwbRoadSectionByMunicipalityId(int municipalityId) {
        return accessibilityNwbRoadSections.values().stream()
                .filter(accessibilityNwbRoadSection -> accessibilityNwbRoadSection.municipalityId().equals(municipalityId))
                .toList();
    }

    public List<AccessibilityNwbRoadSection> findAllAccessibilityNwbRoadSection() {
        return accessibilityNwbRoadSections.values().stream().toList();
    }
}
