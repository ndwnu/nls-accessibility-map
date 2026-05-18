package nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Collection;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Builder
public class StreamQueueProperties {

    @NotBlank
    private String streamTrackingName;

    @NotNull
    private Boolean listenerAutoStart;

    @NotBlank
    private String streamQueueName;

    @Positive
    private int maxAgeInDays;

    @Positive
    private int maxSegmentSizeInKb;

    @NotNull
    @Valid
    private StreamConsumerProperties consumer;

    @Valid
    @Builder.Default
    private Collection<String> messagesToSkip;

    public boolean isMessageToSkip(Object id) {
        if (Objects.isNull(messagesToSkip)) {
            return false;
        }
        // Skip messages without a messageId
        // or check the list from the configuration if the message should be skipped
        return Objects.isNull(id)
                || (messagesToSkip.contains(id.toString()));
    }
}
