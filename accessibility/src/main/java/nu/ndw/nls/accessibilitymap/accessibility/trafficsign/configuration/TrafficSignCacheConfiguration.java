package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.accessibilitymap.traffic-signs.cache")
@Validated
@SuperBuilder
@NoArgsConstructor
public class TrafficSignCacheConfiguration extends CacheConfiguration {

}
