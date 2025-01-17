package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Getter
@Validated
public final class AccessibilityRequest {

    private final Integer municipalityId;

    @NotNull
    private final Double searchRadiusInMeters;

    @Valid
    private final VehicleProperties vehicleProperties;

    @NotNull
    private final List<TrafficSignType> trafficSignTypes;

    private final boolean includeOnlyTimeWindowedSigns;

    @NotNull
    private final Double startLocationLatitude;

    @NotNull
    private final Double startLocationLongitude;
}
