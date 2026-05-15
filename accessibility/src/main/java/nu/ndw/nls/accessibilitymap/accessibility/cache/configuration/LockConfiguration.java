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

    private static final int LOCK_TIMEOUT_SECONDS = 60;

    private static final int RETRY_INTERVAL_MS = 500;

    Duration defaultLockTtl = Duration.ofSeconds(LOCK_TIMEOUT_SECONDS);

    Duration lockRetryInterval = Duration.ofMillis(RETRY_INTERVAL_MS);
}
