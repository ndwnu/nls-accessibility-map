package nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibility-map")
@Getter
@Setter
@Validated
public class AccessibilityMapApiConfiguration {

    @NotNull
    private String host;

    @NotNull
    private Integer port;

    @NotNull
    private Duration awaitDuration = Duration.ofMinutes(1);

}
