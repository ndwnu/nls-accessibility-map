package nu.ndw.nls.accessibilitymap.jobs;

import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.routingmapmatcher.config.MapMatcherConfiguration;
import nu.ndw.nls.springboot.messaging.MessagingConfig;
import nu.ndw.nls.springboot.messaging.properties.validation.MessagingRequiredConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MapMatcherConfiguration.class, MessagingConfig.class, MessagingConfig.class})
@MessagingRequiredConfiguration(receive = {}, publish = {NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED})
public class AccessibilityMapConfiguration {

}
