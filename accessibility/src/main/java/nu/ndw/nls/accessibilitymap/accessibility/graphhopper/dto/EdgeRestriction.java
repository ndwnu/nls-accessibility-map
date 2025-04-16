package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;

@Builder
@Getter
@EqualsAndHashCode
public class EdgeRestriction {

    Integer edgeKey;

    TrafficSign trafficSign;
}
