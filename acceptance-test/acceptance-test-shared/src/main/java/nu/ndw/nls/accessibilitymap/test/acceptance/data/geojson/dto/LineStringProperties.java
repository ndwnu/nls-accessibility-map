package nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.LineStringGraphHopperGraphProperties;

@Getter
@SuperBuilder
public class LineStringProperties extends LineStringGraphHopperGraphProperties {

    private final Integer municipalityCode;
}
