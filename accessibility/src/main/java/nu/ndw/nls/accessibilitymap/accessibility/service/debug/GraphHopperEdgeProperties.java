package nu.ndw.nls.accessibilitymap.accessibility.service.debug;

import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.Properties;

@Getter
@Builder
public class GraphHopperEdgeProperties implements Properties {

    private final String type = "edge";

    private final int edge;

    private final int edgeKey;

    private final int fromNode;

    private final int toNode;

    private final double distance;
}
