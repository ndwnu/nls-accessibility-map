package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.docker.configuration;

import jakarta.annotation.Nonnull;
import java.io.File;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.test.component.driver.docker")
public class DockerDriverConfiguration {

    @Nonnull
    private File composeFile;

}
