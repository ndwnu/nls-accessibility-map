package nu.ndw.nls.accessibilitymap.jobs.test.component.core.configuration;

import jakarta.validation.constraints.NotNull;
import java.time.ZoneId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.test.component.general")
@Validated
public class GeneralConfiguration {

    @NotNull
    private ZoneId timeZone;

    private boolean waitForDebuggerToBeConnected;
}
