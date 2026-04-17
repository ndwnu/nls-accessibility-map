package nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.dto;

public record MessagingStatus(
        boolean enabled,
        int messagesProcessed,
        int messagesRejected) {

}
