package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record AnalyseProperties(
        @NotNull String name,
        @NotNull Integer nwbVersion,
        @NotNull Boolean reportIssues,
        @NotNull OffsetDateTime startTime,
        @NotNull VehicleProperties vehicleProperties,
        @NotNull TrafficSignType trafficSignType,
        @Min(50) @Max(54) double startLocationLatitude,
        @Min(3) @Max(8) double startLocationLongitude,
        @Min(1) double searchRadiusInMeters) {

}
