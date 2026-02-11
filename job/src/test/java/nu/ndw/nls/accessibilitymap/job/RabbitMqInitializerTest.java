package nu.ndw.nls.accessibilitymap.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import nu.ndw.nls.springboot.messaging.services.MessagingDeclareAndBindConfigurationService;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

@ExtendWith(MockitoExtension.class)
class RabbitMqInitializerTest {

    private RabbitMqInitializer rabbitMqInitializer;

    @Mock
    private MessagingDeclareAndBindConfigurationService messagingDeclareAndBindConfigurationService;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        rabbitMqInitializer = new RabbitMqInitializer(messagingDeclareAndBindConfigurationService);
    }

    @Test
    void initRabbitMq() {

        rabbitMqInitializer.initRabbitMq();

        verify(messagingDeclareAndBindConfigurationService).initialize();
        loggerExtension.containsLog(Level.INFO, "Initializing RabbitMQ");
        loggerExtension.containsLog(Level.INFO, "RabbitMQ channel successfully initialized");
    }

    @Test
    void initRabbitMq_annotations() {
        AnnotationUtil.methodContainsAnnotation(
                rabbitMqInitializer.getClass(),
                EventListener.class,
                "initRabbitMq",
                eventListener -> assertThat(eventListener.value()).containsExactly(ApplicationStartedEvent.class));
    }
}
