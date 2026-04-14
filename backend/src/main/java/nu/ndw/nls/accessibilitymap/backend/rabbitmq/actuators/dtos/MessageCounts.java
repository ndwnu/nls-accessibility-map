package nu.ndw.nls.accessibilitymap.backend.rabbitmq.actuators.dtos;

public record MessageCounts(int messagesProcessed, int messagesRejected) {

}
