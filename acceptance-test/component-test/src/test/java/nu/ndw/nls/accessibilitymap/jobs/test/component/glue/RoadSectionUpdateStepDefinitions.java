package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.test.acceptance.core.util.FileService;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.AccessibilityMapApiClient;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.dto.MessagingStatus;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.rabbitmq.RabbitMQStreamDriver;

@Slf4j
@RequiredArgsConstructor
public class RoadSectionUpdateStepDefinitions {

    private final RabbitMQStreamDriver rabbitMQStreamDriver;

    private final AccessibilityMapApiClient accessibilityMapApiClient;

    private final FileService fileService;

    @Given("reset listener counts")
    public void resetListenerCounts() {
        accessibilityMapApiClient.resetListenerCounter("updateRoadSectionStreamListener");
    }

    @Given("a road section update event {word}")
    public void aRoadSectionUpdateEvent(String messageFile) {
        String message = fileService.readTestDataFromFile("nwb-updates", messageFile, "json");
        rabbitMQStreamDriver.publishEvent(message);
    }

    @Given("a road section update event {word} with messageId {word}")
    public void aRoadSectionUpdateEventWithMessageId(String message, String messageId) {
        if (messageId.equals("null")) {
            rabbitMQStreamDriver.publishEvent(message, null);
        } else {
            rabbitMQStreamDriver.publishEvent(message, messageId);
        }
    }

    @When("message is processed and processed success count is {int} and rejected count is {int}")
    public void eventIsProcessed(int success, int rejected) {

        await().atMost(Duration.ofMillis(25000))
                .untilAsserted(() -> {
                    MessagingStatus status = accessibilityMapApiClient.getMessagingStatus("updateRoadSectionStreamListener").body();
                    log.info("messaging status: {}", status);
                    assertThat(status.messagesProcessed()).isEqualTo(success);
                    assertThat(status.messagesRejected()).isEqualTo(rejected);
                });
    }

    @When("a new nwb version is imported")
    public void aNewNwbVersionIsPublished() {

    }
}
