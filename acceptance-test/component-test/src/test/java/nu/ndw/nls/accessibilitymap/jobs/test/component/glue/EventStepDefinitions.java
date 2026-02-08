package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.rabbitmq.RabbitMQMessageDriver;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventSubject;
import nu.ndw.nls.events.NlsEventSubjectType;
import nu.ndw.nls.events.NlsEventType;

@RequiredArgsConstructor
public class EventStepDefinitions {

    private final RabbitMQMessageDriver rabbitMQMessageDriver;

    @Given("a network updated event is triggerd")
    public void triggerNetworkUpdatedEvent() {

        rabbitMQMessageDriver.publishEvent(NlsEvent.builder()
                .type(NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED)
                .subject(NlsEventSubject.builder()
                        .type(NlsEventSubjectType.ACCESSIBILITY_ROUTING_NETWORK)
                        .nwbVersion("123")
                        .timestamp(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .build())
                .build());
    }
}
