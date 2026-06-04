package nu.ndw.nls.accessibilitymap.accessibility.service.debug;

import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.Properties;

@Getter
@Builder
public class GraphHopperNodeProperties implements Properties {

    private final int id;

    private final String type = "node";
}
