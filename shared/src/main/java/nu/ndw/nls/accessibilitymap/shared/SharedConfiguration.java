package nu.ndw.nls.accessibilitymap.shared;

import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GraphHopperProperties.class)
public class SharedConfiguration {

}
