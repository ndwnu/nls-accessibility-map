package nu.ndw.nls.accessibilitymap.job.trafficsign.emission.client.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ConfigurationProperties(prefix = "nu.ndw.nls.accessibilitymap.traffic-signs.emission-zone.client.oauth2")
@Configuration
public class EmissionZoneOAuthConfiguration {

    private String registrationId;
}
