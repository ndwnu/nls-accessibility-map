package nu.ndw.nls.accessibilitymap.jobs.test.component.driver;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.test.component.driver.general")
@Validated
public class DriverGeneralConfiguration {

    private Path debugFolder;
}
