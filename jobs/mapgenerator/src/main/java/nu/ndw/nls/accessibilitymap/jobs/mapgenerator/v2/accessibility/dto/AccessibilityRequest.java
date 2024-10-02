package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto;

import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSignType;
import org.locationtech.jts.geom.Point;

@Builder
@With
@Getter
public final class AccessibilityRequest {

    @NonNull
    private final Point startPoint;

    @NonNull
    private final Integer municipalityId;

    @NonNull
    private final Double searchDistanceInMetres;

    @NonNull
    private final VehicleProperties vehicleProperties;

    @NonNull
    private final Integer nwbVersion;

    @NotNull
    @Default
    private final Set<TrafficSignType> trafficSigns = new HashSet<>();
}
