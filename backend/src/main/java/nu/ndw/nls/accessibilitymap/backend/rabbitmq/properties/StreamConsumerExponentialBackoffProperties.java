package nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties;

import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Builder
public class StreamConsumerExponentialBackoffProperties {

    @Positive
    private int initialIntervalInSeconds;

    @Positive
    private double multiplier;

    @Positive
    private int maxIntervalInSeconds;
}
