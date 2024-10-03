package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import org.locationtech.jts.geom.Point;

@Builder
@With
@Getter
public final class AccessibilityRequest {

    @NonNull
    private final Point startPoint;

    private final Integer municipalityId;

    @NonNull
    private final Double searchDistanceInMetres;

    private final VehicleProperties vehicleProperties;

    @NotNull
    private final TrafficSignType trafficSignType;

    @NotNull
    private final boolean includeOnlyTimeWindowedSigns;
}
