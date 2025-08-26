package nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;

@Getter
@Builder
public class LineStringProperties implements Properties {

    private final long roadSectionId;

    private final List<Direction> directions;

    private final long fromNodeId;

    private final long toNodeId;

    private final int edge;

    private final int edgeKey;

    private final int reverseEdgeKey;
}
