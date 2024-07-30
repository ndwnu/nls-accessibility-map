package nu.ndw.nls.accessibilitymap.shared;

import nu.ndw.nls.geometry.GeometryConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({GeometryConfiguration.class})
@Configuration
@ComponentScan
public class SharedConfiguration {

}
