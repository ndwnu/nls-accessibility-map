package nu.ndw.nls.accessibilitymap.accessibility.network.configuration;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.accessibilitymap.network.cache")
@Validated
@SuperBuilder
@NoArgsConstructor
@Getter
public class NetworkCacheConfiguration extends CacheConfiguration {

    @Default
    private boolean writeDataOnStartup = false;
}
