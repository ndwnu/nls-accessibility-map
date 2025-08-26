package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper;

import jakarta.annotation.Nonnull;
import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.test.component.driver.graph-hopper")
@Validated
public class GraphHopperConfiguration {

    @Nonnull
    private Path locationOnDisk;

}
