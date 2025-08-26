package nu.ndw.nls.accessibilitymap.test.performance.configuration;

import java.time.Duration;
import java.util.Optional;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
public record Section(
        String name,
        @DefaultValue("") Optional<Duration> responseTimeMax,
        @DefaultValue("") Optional<Duration> responseTime99thPercentile,
        @DefaultValue("") Optional<Duration> responseTime95thPercentile,
        @DefaultValue("") Optional<Duration> responseTime75thPercentile,
        @DefaultValue("") Optional<Duration> responseTime50thPercentile) {

}
