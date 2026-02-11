package nu.ndw.nls.accessibilitymap.job.dataanalyser.command.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.With;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record AnalyseNetworkConfiguration(
        @NotNull String name,
        @NotNull Boolean reportIssues,
        @NotNull Double searchRadiusInMeters,
        @NotNull Double startLocationLatitude,
        @NotNull Double startLocationLongitude) {

}
