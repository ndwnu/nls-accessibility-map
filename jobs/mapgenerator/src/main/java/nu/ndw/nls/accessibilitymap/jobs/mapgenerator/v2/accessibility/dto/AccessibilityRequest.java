package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;

@Builder
@With
@Getter
public final class AccessibilityRequest {

    private final Integer municipalityId;

    @NonNull
    private final Double searchDistanceInMetres;

    private final VehicleProperties vehicleProperties;

    @NonNull
    private final TrafficSignType trafficSignType;

    private final boolean includeOnlyTimeWindowedSigns;

    @NonNull
    private final Double startLocationLatitude;

    @NonNull
    private final Double startLocationLongitude;
}
