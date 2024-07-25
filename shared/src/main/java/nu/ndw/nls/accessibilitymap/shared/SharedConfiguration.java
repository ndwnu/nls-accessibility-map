package nu.ndw.nls.accessibilitymap.shared;

import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperProperties;
import nu.ndw.nls.geometry.GeometryConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Import({GeometryConfiguration.class})
@Configuration
@EnableConfigurationProperties(GraphHopperProperties.class)
@ComponentScan
public class SharedConfiguration {

}
