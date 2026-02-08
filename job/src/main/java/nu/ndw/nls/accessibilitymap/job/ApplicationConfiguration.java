package nu.ndw.nls.accessibilitymap.job;

import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.NwbConfiguration;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignClientConfiguration;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.geojson.geometry.JtsGeoJsonMappersConfiguration;
import nu.ndw.nls.locationdataissuesapi.client.feign.LocationDataIssuesApiClientConfiguration;
import nu.ndw.nls.springboot.core.NlsSpringBootCoreAutoConfiguration;
import nu.ndw.nls.springboot.datadog.DatadogConfiguration;
import nu.ndw.nls.springboot.messaging.MessagingConfig;
import nu.ndw.nls.springboot.messaging.properties.validation.MessagingRequiredConfiguration;
import nu.ndw.nls.springboot.security.oauth2.client.OAuthClientCredentialsConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@EnableFeignClients
@Import({AccessibilityConfiguration.class,
        DatadogConfiguration.class,
        TrafficSignClientConfiguration.class,
        JtsGeoJsonMappersConfiguration.class,
        OAuthClientCredentialsConfiguration.class,
        NwbConfiguration.class,
        NlsSpringBootCoreAutoConfiguration.class,
        LocationDataIssuesApiClientConfiguration.class,
        MessagingConfig.class})
@MessagingRequiredConfiguration(receive = {}, publish = {NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED})
public class ApplicationConfiguration {

}
