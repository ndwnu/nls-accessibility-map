package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityNwbRoadSectionToLinkMapper {

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    public AccessibilityLink map(AccessibilityNwbRoadSection accessibilityNwbRoadSection) {

        return AccessibilityLink.builder()
                .id(accessibilityNwbRoadSection.roadSectionId())
                .fromNodeId(accessibilityNwbRoadSection.fromNode())
                .toNodeId(accessibilityNwbRoadSection.toNode())
                .geometry(accessibilityNwbRoadSection.geometry())
                .municipalityCode(accessibilityNwbRoadSection.municipalityId())
                .distanceInMeters(fractionAndDistanceCalculator.calculateLengthInMeters(accessibilityNwbRoadSection.geometry()))
                .accessibility(DirectionalDto.<Boolean>builder()
                        .forward(accessibilityNwbRoadSection.forwardAccessible())
                        .reverse(accessibilityNwbRoadSection.backwardAccessible())
                        .build())
                .build();
    }
}
