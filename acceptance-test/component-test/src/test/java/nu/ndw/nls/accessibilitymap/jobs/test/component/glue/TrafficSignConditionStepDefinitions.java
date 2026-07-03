package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static org.hsqldb.lib.tar.TarHeaderField.name;

import io.cucumber.java.en.Given;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.emission.EmissionZoneDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.TrafficSignConditionDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.TrafficSignDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.TrafficSignTestDataService;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSign;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSignConditionDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.CategoryEnum;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.VehicleTypeEnum;

@Slf4j
@RequiredArgsConstructor
public class TrafficSignConditionStepDefinitions {

    private final TrafficSignConditionDriver trafficSignConditionDriver;

    @Given("with traffic sign condition(s)")
    public void trafficSigns(List<TrafficSignConditionDto> trafficSigns) {
        for (TrafficSignConditionDto trafficSign : trafficSigns) {
            trafficSignConditionDriver.addCondition(trafficSign.name(), map(trafficSign));
        }
    }

    private ConditionPropertiesDtoV5Json map(TrafficSignConditionDto trafficSignConditionDto) {
        return ConditionPropertiesDtoV5Json.builder()
                .vehicleType(trafficSignConditionDto.vehicleType())
                .category(trafficSignConditionDto.category())
                .timeValidity(trafficSignConditionDto.timeValidity())
                .emissionClass(trafficSignConditionDto.emissionClass())
                .fuelType(trafficSignConditionDto.fuelType())
                .axleWeight(trafficSignConditionDto.axleWeightKg())
                .height(trafficSignConditionDto.heightInCm())
                .length(trafficSignConditionDto.lengthInCm())
                .weight(trafficSignConditionDto.weightInKg())
                .width(trafficSignConditionDto.widthInCm())
                .build();
    }

}
