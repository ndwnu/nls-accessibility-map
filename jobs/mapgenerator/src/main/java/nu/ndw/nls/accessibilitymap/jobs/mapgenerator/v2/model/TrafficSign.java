package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import java.net.URI;
import lombok.Builder;

@Builder
public record TrafficSign(
        TrafficSignType trafficSignType,
        String windowTimes,
        double fraction,
        URI iconUri) {
}
