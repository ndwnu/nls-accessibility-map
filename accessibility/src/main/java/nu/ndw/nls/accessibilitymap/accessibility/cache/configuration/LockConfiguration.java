package nu.ndw.nls.accessibilitymap.accessibility.cache.configuration;

import java.time.Duration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.accessibilitymap.network.cache.locking")
@NoArgsConstructor
@Validated
@Getter
@Setter
public class LockConfiguration {

    Duration defaultLockTtl = Duration.ofSeconds(60);

    Duration lockRetryInterval = Duration.ofMillis(500);
}
