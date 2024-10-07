package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model;

import lombok.Builder;
import lombok.Value;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;

@Value
@Builder
public class RoadSectionProperties implements Properties {

    long nwbRoadSectionId;

    boolean accessible;

    Direction direction;
}
