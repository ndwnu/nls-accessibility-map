package nu.ndw.nls.accessibilitymap.backend;

import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.configuration.GraphHopperProperties;
import nu.ndw.nls.springboot.datadog.DatadogConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({DatadogConfiguration.class, AccessibilityConfiguration.class})
@EnableConfigurationProperties(GraphHopperProperties.class)
public class AccessibilityMapConfiguration {

}
