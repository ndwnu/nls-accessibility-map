package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model;

import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.Direction;

@Getter
@Builder
public class RoadSectionProperties implements Properties {

    private final long nwbRoadSectionId;

    private final boolean accessible;

    private final Direction direction;
}
