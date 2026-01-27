package nu.ndw.nls.accessibilitymap.jobs.graphhopper.services.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityNwbRoadSectionToLinkMapper {

    public AccessibilityLink map(AccessibilityNwbRoadSection accessibilityNwbRoadSection) {
        return AccessibilityLink.builder()
                .id(accessibilityNwbRoadSection.roadSectionId())
                .fromNodeId(accessibilityNwbRoadSection.fromNode())
                .toNodeId(accessibilityNwbRoadSection.toNode())
                .geometry(accessibilityNwbRoadSection.geometry())
                .municipalityCode(accessibilityNwbRoadSection.municipalityId())
                .distanceInMeters(accessibilityNwbRoadSection.geometry().getLength())
                .accessibility(DirectionalDto.<Boolean>builder()
                        .forward(accessibilityNwbRoadSection.forwardAccessible())
                        .reverse(accessibilityNwbRoadSection.backwardAccessible())
                        .build())
                .build();
    }
}
