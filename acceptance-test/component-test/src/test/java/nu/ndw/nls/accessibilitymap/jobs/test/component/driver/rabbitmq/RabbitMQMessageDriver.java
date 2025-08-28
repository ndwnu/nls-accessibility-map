package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RabbitMQMessageDriver implements StateManagement {

    private final AmqpAdmin amqpAdmin;

    private final ObjectMapper objectMapper;

    private final MessageService messageService;

    public void publishEvent(NlsEvent nlsEvent) {

        messageService.publish(nlsEvent);
    }

    @Override
    public void clearState() {
        // No state to clear.
    }
}
