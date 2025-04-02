package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.trafficsign;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotEmpty;
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

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.test.component.driver.traffic-sign-cache")
@Validated
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrafficSignCacheDriverConfiguration {

    @Nonnull
    private Path locationOnDisk;

    @NotEmpty
    private String fileNameActiveVersion;

    public File getActiveVersion() {
        return locationOnDisk.resolve(fileNameActiveVersion).toFile();
    }

}
