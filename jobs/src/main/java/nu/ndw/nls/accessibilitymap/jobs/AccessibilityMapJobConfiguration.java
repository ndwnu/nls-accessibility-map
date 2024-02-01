package nu.ndw.nls.accessibilitymap.jobs;

import nu.ndw.nls.accessibilitymap.shared.SharedConfiguration;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import nu.ndw.nls.springboot.messaging.MessagingConfig;
import nu.ndw.nls.springboot.messaging.properties.validation.MessagingRequiredConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({RoutingMapMatcherConfiguration.class, MessagingConfig.class, MessagingConfig.class, SharedConfiguration.class})
@MessagingRequiredConfiguration(receive = {}, publish = {NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED})
public class AccessibilityMapJobConfiguration {

}
