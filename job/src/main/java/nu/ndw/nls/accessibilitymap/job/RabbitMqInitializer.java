package nu.ndw.nls.accessibilitymap.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.springboot.messaging.services.MessagingDeclareAndBindConfigurationService;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMqInitializer {
    private final MessagingDeclareAndBindConfigurationService service;

    @EventListener(ApplicationStartedEvent.class)
    public void loadDataOnStartup() {

        log.info("Initializing RabbitMQ");
        this.service.initialize();
        log.info("RabbitMQ channel successfully initialized");
    }
}
