package nu.ndw.nls.accessibilitymap.shared;

import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperProperties;
import nu.ndw.nls.geometry.GeometryConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({GeometryConfiguration.class})
@Configuration
@EnableConfigurationProperties(GraphHopperProperties.class)
public class SharedConfiguration {

}
