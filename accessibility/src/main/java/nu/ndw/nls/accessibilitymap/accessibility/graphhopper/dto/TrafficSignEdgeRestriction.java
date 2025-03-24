package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class TrafficSignEdgeRestriction {

    Integer edgeKey;
    Integer trafficSignId;
}
