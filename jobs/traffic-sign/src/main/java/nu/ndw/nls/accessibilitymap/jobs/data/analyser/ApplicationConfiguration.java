package nu.ndw.nls.accessibilitymap.jobs.data.analyser;

import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignClientConfiguration;
import nu.ndw.nls.geojson.geometry.JtsGeoJsonMappersConfiguration;
import nu.ndw.nls.springboot.datadog.DatadogConfiguration;
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
        OAuthClientCredentialsConfiguration.class})
public class ApplicationConfiguration {

}
