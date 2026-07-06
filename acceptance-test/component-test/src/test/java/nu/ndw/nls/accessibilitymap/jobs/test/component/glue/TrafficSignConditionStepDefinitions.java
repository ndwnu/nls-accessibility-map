package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.TrafficSignConditionDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSignCondition;

@Slf4j
@RequiredArgsConstructor
public class TrafficSignConditionStepDefinitions {

    private final TrafficSignConditionDriver trafficSignConditionDriver;

    @Given("with traffic sign condition(s)")
    public void trafficSigns(List<TrafficSignCondition> trafficSigns) {
        trafficSignConditionDriver.addTraficSignConditions(trafficSigns);
    }

}
