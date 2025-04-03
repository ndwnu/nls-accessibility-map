package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.accessibilitymap.trafficsigns.cache")
@Validated
@Builder
@Getter
@Setter
public class TrafficSignCacheConfiguration {

    private boolean failOnNoDataOnStartup = true;

    @NotNull
    private Path folder;

    @NotEmpty
    private String fileNameActiveVersion;

    public File getActiveVersion() {
        return folder.resolve(fileNameActiveVersion).toFile();
    }
}
