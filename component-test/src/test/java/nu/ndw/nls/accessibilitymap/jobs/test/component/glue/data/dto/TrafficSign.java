package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto;

import lombok.Builder;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;

@Builder
public record TrafficSign(
        String id,
        int startNodeId,
        int endNodeId,
        double fraction,
        String rvvCode,
        String blackCode,
        DirectionType directionType,
        String windowTime,
        String emissionZoneId) {

}
