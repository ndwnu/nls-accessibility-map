package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto;

import lombok.Builder;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json.DrivingDirectionEnum;

@Builder
public record TrafficSign(
        String id,
        int startNodeId,
        int endNodeId,
        double fraction,
        String rvvCode,
        String blackCode,
        DrivingDirectionEnum directionType,
        String windowTime,
        String regulationOrderId) {

}
