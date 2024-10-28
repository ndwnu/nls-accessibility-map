package nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LineStringProperties implements Properties{

    private final long roadSectionId;

    private final long fromNodeId;

    private final long toNodeId;
}
