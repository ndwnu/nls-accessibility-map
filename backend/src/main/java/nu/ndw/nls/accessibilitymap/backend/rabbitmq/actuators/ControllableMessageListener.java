package nu.ndw.nls.accessibilitymap.backend.rabbitmq.actuators;

public interface ControllableMessageListener {

    String getListenerId();

    void resetCounters();

    int getMessagesProcessed();

    int getMessagesRejected();
}
