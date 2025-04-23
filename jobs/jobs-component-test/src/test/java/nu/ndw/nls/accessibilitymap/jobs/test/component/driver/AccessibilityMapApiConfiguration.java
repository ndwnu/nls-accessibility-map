package nu.ndw.nls.accessibilitymap.jobs.test.component.driver;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.test.component.driver.accessibility-map")
@Getter
@Setter
@Validated
public class AccessibilityMapApiConfiguration {

    @NotNull
    private String host;

    @NotNull
    private Integer port;

}
