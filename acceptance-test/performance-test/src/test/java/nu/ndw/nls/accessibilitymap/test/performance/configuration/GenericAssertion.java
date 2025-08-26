package nu.ndw.nls.accessibilitymap.test.performance.configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
public record GenericAssertion(
        @DefaultValue("") Optional<Duration> scenarioDurationMax,
        @DefaultValue("") Optional<Duration> scenarioDuration99thPercentile,
        @DefaultValue("") Optional<Duration> scenarioDuration95thPercentile,
        @DefaultValue("") Optional<Duration> scenarioDuration75thPercentile,
        @DefaultValue("") Optional<Duration> scenarioDuration50thPercentile,
        @Min(0) @Max(100) BigDecimal successfulRunPercentage) {

}
