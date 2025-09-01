package nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointNodeProperties implements Properties {

    private final String name;

    private final long nodeId;
}
