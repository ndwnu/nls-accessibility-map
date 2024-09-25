package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record TrafficSign(
        @NotNull TrafficSignType trafficSignType,
        @NotNull String windowTimes,
        @NotNull Double nwbFraction) {

}
