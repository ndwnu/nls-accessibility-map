package nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "nu.ndw.nls.accessibilitymap.messaging")
@Validated
@Getter
@Builder
@Setter
public class MessageStreamProperties {

    private Map<String, @Valid StreamQueueProperties> streamQueues;
}
