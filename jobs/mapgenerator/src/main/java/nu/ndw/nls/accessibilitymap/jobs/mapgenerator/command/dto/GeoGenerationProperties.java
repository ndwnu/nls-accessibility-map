package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.trafficsign.TrafficSignType;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record GeoGenerationProperties(
        int exportVersion,
        int nwbVersion,
        boolean publishEvents,
        @NotNull TrafficSignType trafficSignType,
        @NotNull VehicleProperties vehicleProperties,
        @NotNull boolean includeOnlyTimeWindowedSigns,
        @NotNull GeoJsonProperties geoJsonProperties,
        @Min(50) @Max(54) double startLocationLatitude,
        @Min(3) @Max(8) double startLocationLongitude,
        @Min(1) double searchRadiusInMeters) {

}
