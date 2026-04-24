package nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Builder
public class StreamConsumerProperties {

    @Positive
    private int flushIntervalInSeconds;

    @Positive
    private int messageCountBeforeStorage;

    @NotNull
    private StreamConsumerExponentialBackoffProperties exponentialBackoff;
}
