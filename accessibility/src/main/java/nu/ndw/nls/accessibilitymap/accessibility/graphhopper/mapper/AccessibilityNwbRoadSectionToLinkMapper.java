package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.repository.NwbRoadSectionGeometryRepository;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityNwbRoadSectionToLinkMapper {

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    private final NwbRoadSectionGeometryRepository nwbRoadSectionGeometryRepository;

    public AccessibilityLink map(int versionId, AccessibilityNwbRoadSection accessibilityNwbRoadSection) {

        LineString geometry = nwbRoadSectionGeometryRepository.findGeometryById(versionId,
                accessibilityNwbRoadSection.roadSectionId());

        return AccessibilityLink.builder()
                .id(accessibilityNwbRoadSection.roadSectionId())
                .fromNodeId(accessibilityNwbRoadSection.fromNode())
                .toNodeId(accessibilityNwbRoadSection.toNode())
                .geometry(geometry)
                .municipalityCode(accessibilityNwbRoadSection.municipalityId())
                .distanceInMeters(fractionAndDistanceCalculator.calculateLengthInMeters(geometry))
                .accessibility(DirectionalDto.<Boolean>builder()
                        .forward(accessibilityNwbRoadSection.forwardAccessible())
                        .reverse(accessibilityNwbRoadSection.backwardAccessible())
                        .build())
                .build();
    }
}
