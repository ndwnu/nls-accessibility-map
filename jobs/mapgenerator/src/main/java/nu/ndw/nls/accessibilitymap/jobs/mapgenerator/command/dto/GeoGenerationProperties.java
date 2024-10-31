package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record GeoGenerationProperties(
        @NotNull Integer exportVersion,
        @NotNull Integer nwbVersion,
        @NotNull Boolean publishEvents,
        @NotNull OffsetDateTime startTime,
        @NotNull TrafficSignType trafficSignType,
        @NotNull VehicleProperties vehicleProperties,
        @NotNull Boolean includeOnlyTimeWindowedSigns,
        @NotNull GenerateConfiguration generateConfiguration,
        @Positive double polygonMaxDistanceBetweenPoints,
        @Min(50) @Max(54) double startLocationLatitude,
        @Min(3) @Max(8) double startLocationLongitude) {

}
