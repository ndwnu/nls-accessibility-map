package nu.ndw.nls.accessibilitymap.accessibility.cache.configuration;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.File;
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

    private static final int DEFAULT_CACHE_TTL = 60;

    @NotNull
    private String name;

    @Default
    private boolean loadDataOnStartup = true;

    @Default
    private boolean failOnStartupCacheReadError = true;

    @Default
    private boolean watchForUpdates = true;

    @NotNull
    private Path folder;

    @NotEmpty
    @Default
    private String fileNameActiveVersion = "active";

    @Default
    private Duration fileWatcherInterval = Duration.ofSeconds(1);

    @Default
    private int acceptableConsequentReadFailures = 1;

    private Duration maxLockWaitTime = Duration.ofSeconds(DEFAULT_CACHE_TTL);

    public File getActiveVersion() {
        return folder.resolve(fileNameActiveVersion).toFile();
    }
}
