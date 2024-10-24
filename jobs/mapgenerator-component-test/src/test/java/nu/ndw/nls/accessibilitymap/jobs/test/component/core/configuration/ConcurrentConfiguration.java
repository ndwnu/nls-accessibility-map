package nu.ndw.nls.accessibilitymap.jobs.test.component.core.configuration;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.test.component.concurrent")
public class ConcurrentConfiguration {

    private Duration threadTerminationTimeout = Duration.ofSeconds(10);
}
