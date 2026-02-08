package nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.dto;

import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;

@Getter
@Builder
public class RoadSectionProperties implements Properties {

    private final long nwbRoadSectionId;

    private final boolean accessible;

    private final Direction direction;

    private final long roadSectionFragmentId;
}
