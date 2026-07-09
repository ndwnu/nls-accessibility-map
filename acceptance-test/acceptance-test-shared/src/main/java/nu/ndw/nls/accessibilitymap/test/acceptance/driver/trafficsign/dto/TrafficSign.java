package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto;

import java.util.List;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json.DrivingDirectionEnum;

@Builder
public record TrafficSign(
        String id,
        int startNodeId,
        int endNodeId,
        double fraction,
        String rvvCode,
        TrafficSignCondition restrictions,
        List<TrafficSignCondition> exemptions,
        List<SupplementaryTrafficSign> supplementaryTrafficSigns,
        String blackCode,
        DrivingDirectionEnum directionType,
        String windowTime,
        String regulationOrderId) {

}
