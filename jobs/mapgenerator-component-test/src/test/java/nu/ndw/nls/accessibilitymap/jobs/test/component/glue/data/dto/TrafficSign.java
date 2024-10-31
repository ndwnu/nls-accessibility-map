package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto;

import lombok.Builder;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;

@Builder
public record TrafficSign(
        int startNodeId,
        int endNodeId,
        double fraction,
        String rvvCode,
        DirectionType directionType,
        String windowTime) {

}
