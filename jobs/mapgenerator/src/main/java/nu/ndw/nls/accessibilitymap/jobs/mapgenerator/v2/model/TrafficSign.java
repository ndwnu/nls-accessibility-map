package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import java.net.URI;
import lombok.Builder;

@Builder
public record TrafficSign(
        int roadSectionId,
        TrafficSignType trafficSignType,
        double latitude,
        double longitude,
        Direction direction,
        String windowTimes,
        double fraction,
        URI iconUri) {
}
