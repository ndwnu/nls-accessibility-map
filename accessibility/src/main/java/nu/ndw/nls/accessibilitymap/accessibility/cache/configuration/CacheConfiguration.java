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

    @NotNull
    private String name;

    @Default
    private boolean loadDataOnStartup = true;

    @Default
    private boolean failOnCacheReadError = true;

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

    public File getActiveVersion() {
        return folder.resolve(fileNameActiveVersion).toFile();
    }
}
