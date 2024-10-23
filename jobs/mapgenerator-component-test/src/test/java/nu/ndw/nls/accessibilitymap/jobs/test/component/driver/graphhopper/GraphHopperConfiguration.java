package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper;

import jakarta.annotation.Nonnull;
import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.test.component.driver.graph-hopper")
public class GraphHopperConfiguration {

    @Nonnull
    private Path locationOnDisk;

}
