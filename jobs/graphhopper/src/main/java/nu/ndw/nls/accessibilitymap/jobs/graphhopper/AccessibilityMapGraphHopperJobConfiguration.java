package nu.ndw.nls.accessibilitymap.jobs.graphhopper;

import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.NwbConfiguration;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignClientConfiguration;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.springboot.core.NlsSpringBootCoreAutoConfiguration;
import nu.ndw.nls.springboot.datadog.DatadogConfiguration;
import nu.ndw.nls.springboot.messaging.MessagingConfig;
import nu.ndw.nls.springboot.messaging.properties.validation.MessagingRequiredConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        AccessibilityConfiguration.class,
        NwbConfiguration.class,
        TrafficSignClientConfiguration.class,
        NlsSpringBootCoreAutoConfiguration.class,
        MessagingConfig.class,
        DatadogConfiguration.class})
@MessagingRequiredConfiguration(receive = {}, publish = {NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED})
public class AccessibilityMapGraphHopperJobConfiguration {

}
