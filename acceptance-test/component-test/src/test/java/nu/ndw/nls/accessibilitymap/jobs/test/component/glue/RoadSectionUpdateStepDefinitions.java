package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.test.acceptance.core.util.FileService;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.rabbitmq.RabbitMQStreamDriver;

@Slf4j
@RequiredArgsConstructor
public class RoadSectionUpdateStepDefinitions {

    private final RabbitMQStreamDriver rabbitMQStreamDriver;

    private final FileService fileService;


    @Given("a road section update event {word}")
    public void aRoadSectionUpdateEvent(String messageFile) {
        String message = fileService.readTestDataFromFile("nwb-updates", messageFile, "json");
        rabbitMQStreamDriver.publishEvent(message);
    }
}
