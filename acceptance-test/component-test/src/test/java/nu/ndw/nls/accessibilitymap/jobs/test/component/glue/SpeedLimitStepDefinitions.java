package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.GraphHopperDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.speedlimit.SpeedLimitDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.speedlimit.dto.SpeedLimit;

@Slf4j
@RequiredArgsConstructor
public class SpeedLimitStepDefinitions {

    private final SpeedLimitDriver  speedLimitDriver;

    private final GraphHopperDriver graphHopperDriver;

    @Given("with speed limits")
    public void trafficSigns(List<SpeedLimit> speedLimits) {

        speedLimitDriver.stubSpeedLimits(speedLimits, graphHopperDriver.getLastBuiltGraphVersion());
    }
}
