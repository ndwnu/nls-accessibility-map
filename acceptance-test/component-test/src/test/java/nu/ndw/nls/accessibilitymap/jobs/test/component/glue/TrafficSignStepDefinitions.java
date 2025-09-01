package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.emission.EmissionZoneDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.TrafficSignDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.TrafficSignTestDataService;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSign;

@Slf4j
@RequiredArgsConstructor
public class TrafficSignStepDefinitions {

    private final TrafficSignDriver trafficSignDriver;

    private final EmissionZoneDriver emissionZoneDriver;

    private final TrafficSignTestDataService trafficSignTestDataService;


    @Given("with traffic signs")
    public void trafficSigns(List<TrafficSign> trafficSigns) {

        trafficSignDriver.stubTrafficSignRequest(
                trafficSigns.stream()
                        .map(trafficSignTestDataService::createTrafficSignGeoJsonDto)
                        .toList());
        emissionZoneDriver.stubEmissionZone();
    }

}
