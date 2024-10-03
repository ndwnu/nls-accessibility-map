package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import java.net.URI;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record TrafficSign(
        @NonNull Integer roadSectionId,
        @NonNull TrafficSignType trafficSignType,
        @NonNull Double latitude,
        @NonNull Double longitude,
        String windowTimes,
        @NonNull Double fraction,
        URI iconUri) {
}
