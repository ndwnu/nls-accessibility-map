package nu.ndw.nls.accessibilitymap.accessibility.cache.configuration;

import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.time.Duration;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.annotation.Validated;

@NoArgsConstructor
@Validated
@Getter
@Setter
@SuperBuilder
public class CacheConfiguration {

    private static final int DEFAULT_MAX_LOCK_WAIT_TIME = 60;

    @NotNull
    private String name;

    @NotNull
    private Integer cacheVersion;

    @Default
    private boolean loadDataOnStartup = true;

    @Default
    private boolean failOnStartupCacheReadError = true;

    @Default
    private boolean watchForUpdates = true;

    @NotNull
    private Path folder;

    @Default
    private Duration fileWatcherInterval = Duration.ofSeconds(1);

    @Default
    private int acceptableConsequentReadFailures = 1;

    private Duration maxLockWaitTime = Duration.ofSeconds(DEFAULT_MAX_LOCK_WAIT_TIME);
}
