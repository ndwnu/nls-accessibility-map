package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.maven;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.test.component.driver.maven")
public class MavenConfiguration {

    private String moduleUnderTest;

    private String rootPomRelativePath;
}
