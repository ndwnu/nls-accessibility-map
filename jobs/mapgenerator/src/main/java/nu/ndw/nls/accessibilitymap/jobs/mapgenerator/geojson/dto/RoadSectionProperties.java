package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto;

import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.Direction;

@Getter
@Builder
public class RoadSectionProperties implements Properties {

    private final long nwbRoadSectionId;

    private final boolean accessible;

    private final Direction direction;
}
