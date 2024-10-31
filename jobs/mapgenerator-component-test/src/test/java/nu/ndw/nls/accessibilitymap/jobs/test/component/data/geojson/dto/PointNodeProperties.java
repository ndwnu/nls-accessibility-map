package nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointNodeProperties implements Properties {

    private final long nodeId;
}
