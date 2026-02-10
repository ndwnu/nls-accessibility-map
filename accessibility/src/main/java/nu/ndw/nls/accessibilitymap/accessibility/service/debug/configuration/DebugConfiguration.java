package nu.ndw.nls.accessibilitymap.accessibility.service.debug.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.accessibilitymap.accessibility.debug")
@NoArgsConstructor
@Getter
@Setter
public class DebugConfiguration {

    private boolean enabled;

    private Path debugFolder = Paths.get("./.debug");

    public boolean isDisabled() {
        return !enabled;
    }
}
