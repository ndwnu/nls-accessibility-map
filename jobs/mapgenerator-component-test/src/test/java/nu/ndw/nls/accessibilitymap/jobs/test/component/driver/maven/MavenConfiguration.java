package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.maven;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.test.component.driver.maven")
@Validated
public class MavenConfiguration {

    @NotNull
    private String moduleUnderTest;

    @NotNull
    private String rootPomRelativePath;
}
