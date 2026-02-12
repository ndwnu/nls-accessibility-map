package nu.ndw.nls.accessibilitymap.accessibility.service.debug;

import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.Properties;

@Getter
@Builder
public class RoadSectionSegmentProperties implements Properties {

    private final long roadSectionId;

    private final int roadSectionFragmentId;

    private final int edge;

    private final int segmentId;

    private final int edgeKey;

    private final Direction direction;

    private final double startFraction;

    private final double endFraction;

    private final boolean accessible;
}
