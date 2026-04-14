package nu.ndw.nls.accessibilitymap.backend.nwb.messaging;

import com.rabbitmq.stream.Message;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.actuators.ControllableMessageListener;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
// Depends on the template to create the stream queue if it does not exist.
@DependsOn("updateRoadSectionStreamTemplate")
public class RoadSectionUpdateListener implements ControllableMessageListener {

    private static final String LISTENER_ID = "updateRoadSectionStreamListener";

    private final AtomicInteger messagesProcessed = new AtomicInteger();

    private final AtomicInteger messagesRejected = new AtomicInteger();

    @RabbitListener(id = LISTENER_ID,
            queues = "nls_accessibility_map_update_road_section",
            containerFactory = "updateRoadSectionStreamFactory")
    public void handleMessage(Message message) {
        try {
            log.info("handle message: {}", message.getBody());
            // for now to test if skipping of the message processing works it checks for the content to contain broken
            if (new String(message.getBodyAsBinary()).contains("broken")) {
                throw new IllegalStateException("Failed processing message");
            }
        } catch (RuntimeException e) {
            messagesRejected.incrementAndGet();
            throw e;
        }
        messagesProcessed.incrementAndGet();
    }

    @Override
    public String getListenerId() {
        return LISTENER_ID;
    }

    @Override
    public void resetCounters() {
        messagesRejected.set(0);
        messagesProcessed.set(0);
    }

    @Override
    public int getMessagesProcessed() {
        return messagesProcessed.get();
    }

    @Override
    public int getMessagesRejected() {
        return messagesRejected.get();
    }
}
