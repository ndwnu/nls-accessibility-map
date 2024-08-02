package nu.ndw.nls.accessibilitymap.backend;

import nu.ndw.nls.accessibilitymap.shared.SharedConfiguration;
import nu.ndw.nls.accessibilitymap.shared.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperProperties;
import nu.ndw.nls.geometry.GeometryConfiguration;
import nu.ndw.nls.springboot.datadog.DatadogConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({DatadogConfiguration.class, GeometryConfiguration.class, SharedConfiguration.class,
        AccessibilityConfiguration.class})
@EnableConfigurationProperties(GraphHopperProperties.class)
public class AccessibilityMapConfiguration {

}
