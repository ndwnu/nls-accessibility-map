package nu.ndw.nls.accessibilitymap.backend.rabbitmq.actuators;

import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.actuators.dtos.MessageCounts;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "messaging")
@RequiredArgsConstructor
@Slf4j
public class MessagingActuator {

    private static final String LISTENER_WITH_ID_NOT_FOUND = "Listener with id: %s not found";

    private final List<ControllableMessageListener> messageListeners;

    @ReadOperation
    public MessageCounts getMessageCounts(String listenerId) {
        return messageListeners.stream()
                .filter(listenerPredicate(listenerId))
                .findFirst()
                .map(messageListener ->
                        new MessageCounts(
                                messageListener.getMessagesProcessed(),
                                messageListener.getMessagesRejected())
                ).orElseThrow(() -> new IllegalArgumentException(LISTENER_WITH_ID_NOT_FOUND.formatted(listenerId)));
    }

    @WriteOperation
    public MessageCounts resetCounters(String listenerId) {
        return messageListeners.stream()
                .filter(listenerPredicate(listenerId))
                .findFirst()
                .map(messageListener -> {
                    log.info("Reset counters for: {}", listenerId);
                    messageListener.resetCounters();
                    return getMessageCounts(listenerId);
                }).orElseThrow(() -> new IllegalArgumentException(LISTENER_WITH_ID_NOT_FOUND.formatted(listenerId)));
    }

    private Predicate<ControllableMessageListener> listenerPredicate(String listenerId) {
        return messageListener -> messageListener.getListenerId().equals(listenerId);
    }
}
