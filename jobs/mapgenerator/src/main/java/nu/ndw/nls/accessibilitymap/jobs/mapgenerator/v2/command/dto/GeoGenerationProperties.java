package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.command.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Builder
@Validated
public final class GeoGenerationProperties {

    private int exportVersion;

    private int nwbVersion;

    private boolean publishEvents;

    @NotNull
    private TrafficSignType trafficSignType;

    @NotNull
    private final VehicleProperties vehicleProperties;

    @NotNull
    private boolean includeOnlyTimeWindowedSigns;

    @NotNull
    private GeoJsonProperties geoJsonProperties;

    @Min(50)
    @Max(54)
    private double startLocationLatitude;

    @Min(3)
    @Max(8)
    private double startLocationLongitude;

    @Min(1)
    private double searchRadiusInMeters;
}
