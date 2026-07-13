package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.mappers;

import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSignCondition;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json;
import org.springframework.stereotype.Component;

@Component
public class ConditionPropertiesDtoV5JsonMapper {

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
                .length(trafficSignCondition.lengthInM())
                .width(trafficSignCondition.widthInM())
                .height(trafficSignCondition.heightInM())
                .weight(trafficSignCondition.weightInTon())
                .axleWeight(trafficSignCondition.axleWeightInTon())
                .build();
    }
}
