package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;

public class EdgeToAccessibilityLinkMapper {

    public static AccessibilityLink buildFromEdge(nu.ndw.nls.springboot.test.graph.dto.Edge edge) {

        return AccessibilityLink.builder()
                .id(edge.getId())
                .fromNodeId(edge.getFromNode().getId())
                .toNodeId(edge.getToNode().getId())
                .accessibility(DirectionalDto.<Boolean>builder()
                        .forward(edge.isForward())
                        .reverse(edge.isBackward())
                        .build())
                .distanceInMeters(edge.getDistanceInMeters())
                .municipalityCode(1)
                .geometry(edge.getWgs84LineString())
                .build();
    }
}
