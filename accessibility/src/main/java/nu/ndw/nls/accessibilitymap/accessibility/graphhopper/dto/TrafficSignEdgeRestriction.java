package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TrafficSignEdgeRestriction {

    Integer edgeKey;
    Integer trafficSignId;
}
