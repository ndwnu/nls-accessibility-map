package nu.ndw.nls.accessibilitymap.test.acceptance.driver.rabbitmq;

import static java.math.BigInteger.TWO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.rabbitmq.stream.Environment;
import java.time.Duration;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitMQStreamDriver implements StateManagement {

    private final Environment environment;
    private final RabbitStreamTemplate template;
    private final String streamQueueName;

    public RabbitMQStreamDriver(
            Environment environment,
            @Value("${nu.ndw.nls.accessibilitymap.messaging.stream-queue-name}")
            String streamQueueName) {
        this.environment = environment;
        this.streamQueueName = streamQueueName;
        this.template = new RabbitStreamTemplate(environment, streamQueueName);
    }

    public void publishEvent(String message) {
        this.publishEvent(message, UUID.randomUUID().toString());
    }

    public void publishEvent(String message, String messageId) {
        log.info("Send message: '{}'", message);
        template.convertAndSend(message, queueMessage -> {
            queueMessage.getMessageProperties().setMessageId(messageId);
            return queueMessage;
        });
    }

    @Override
    @SneakyThrows
    public void prepareState() {
        await().atMost(Duration.ofSeconds(TWO.intValue()))
                .untilAsserted(() -> assertThat(environment.streamExists(streamQueueName)).isTrue());
    }

    @Override
    public void clearState() {
        // empty
    }
}
