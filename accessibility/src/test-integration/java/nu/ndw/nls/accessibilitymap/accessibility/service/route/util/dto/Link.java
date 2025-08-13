package nu.ndw.nls.accessibilitymap.accessibility.service.route.util.dto;

import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import org.locationtech.jts.geom.LineString;

@Builder
@Getter
public class Link {

    private AccessibilityLink accessibilityLink;

    private LineString rijksDiehoekLineString;

    private LineString wgs84LineString;

    private Node startNode;

    private Node endNode;
}
