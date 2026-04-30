package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.test.acceptance.core.util.FileService;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.AccessibilityMapServicesClient;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.rabbitmq.RabbitMQStreamDriver;
import nu.ndw.nls.springboot.test.component.driver.job.JobDriver;

@Slf4j
@RequiredArgsConstructor
public class RoadSectionUpdateStepDefinitions {

    private final RabbitMQStreamDriver rabbitMQStreamDriver;

    private final FileService fileService;

    private final JobDriver jobDriver;

    private final AccessibilityMapServicesClient accessibilityMapServicesClient;

    @Given("no traffic signs")
    public void clearTrafficSignCache() {
        // create empty traffic signs cache
        jobDriver.run("job", "rebuildTrafficSignCache");
        accessibilityMapServicesClient.reloadCaches();
    }

    @Given("a road section update event {word}")
    public void aRoadSectionUpdateEvent(String messageFile) {
        String message = fileService.readTestDataFromFile("nwb-updates", messageFile, "json");
        rabbitMQStreamDriver.publishEvent(message);
    }
}
