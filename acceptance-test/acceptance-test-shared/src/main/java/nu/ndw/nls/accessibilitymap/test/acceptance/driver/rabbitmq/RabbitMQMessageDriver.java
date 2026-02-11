package nu.ndw.nls.accessibilitymap.test.acceptance.driver.rabbitmq;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventSubject;
import nu.ndw.nls.events.NlsEventSubjectType;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RabbitMQMessageDriver implements StateManagement {

    private final AmqpAdmin amqpAdmin;

    private final MessageService messageService;

    public void publishEvent(NlsEvent nlsEvent) {

        messageService.publish(nlsEvent);
    }

    public void publishNwbImportedEvent() {

        messageService.publish(NlsEvent.builder()
                .type(NlsEventType.NWB_IMPORTED_EVENT)
                .subject(NlsEventSubject.builder()
                        .type(NlsEventSubjectType.NWB_VERSION)
                        .version("not used")
                        .timestamp(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .build())
                .build());
    }

    @Override
    public void clearState() {
        // No state to clear.
    }
}
