package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.configuration;

import jakarta.annotation.Nonnull;
import java.io.File;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.test.component.driver.map-generation-job")
@Validated
public class MapGenerationJobDriverConfiguration {

    @Nonnull
    private File locationOnDisk;

}
