package nu.ndw.nls.accessibilitymap.jobs;

import nu.ndw.nls.accessibilitymap.shared.SharedConfiguration;
import nu.ndw.nls.springboot.datadog.DatadogConfiguration;
import nu.ndw.nls.springboot.messaging.MessagingConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SharedConfiguration.class, MessagingConfig.class, DatadogConfiguration.class})
public class AccessibilityMapGeneratorJobConfiguration {

}
