package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import lombok.Builder;

@Builder
public record TrafficSign(
        TrafficSignType trafficSignType,
        String windowTimes,
        Double nwbFraction) {

}
