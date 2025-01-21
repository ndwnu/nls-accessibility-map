package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.With;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@ConfigurationProperties(prefix = "nu.ndw.nls.accessibilitymap.jobs.analyse")
@Validated
public record AnalyserConfiguration(
        @NotNull Double startLocationLatitude,
        @NotNull Double startLocationLongitude,
        @Min(1) double searchRadiusInMeters) {

}
