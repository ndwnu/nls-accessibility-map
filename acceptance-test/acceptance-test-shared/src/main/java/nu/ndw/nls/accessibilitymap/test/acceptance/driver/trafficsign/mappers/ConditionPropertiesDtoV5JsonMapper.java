package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.mappers;

import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSignCondition;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json;
import org.springframework.stereotype.Component;

@Component
public class ConditionPropertiesDtoV5JsonMapper {
    // @todo: rename units into parameters
    public ConditionPropertiesDtoV5Json map(TrafficSignCondition trafficSignCondition) {

        if (trafficSignCondition == null) {
            return null;
        }

        return ConditionPropertiesDtoV5Json.builder()
                .vehicleType(trafficSignCondition.vehicleType())
                .category(trafficSignCondition.category())
                .timeValidity(trafficSignCondition.timeValidity())
                .emissionClass(trafficSignCondition.emissionClass())
                .fuelType(trafficSignCondition.fuelType())
                .axleWeight(trafficSignCondition.axleWeight())
                .height(trafficSignCondition.height())
                .length(trafficSignCondition.length())
                .weight(trafficSignCondition.weight())
                .width(trafficSignCondition.width())
                .build();
    }
}
