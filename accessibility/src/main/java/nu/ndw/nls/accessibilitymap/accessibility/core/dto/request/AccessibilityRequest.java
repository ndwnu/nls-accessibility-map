package nu.ndw.nls.accessibilitymap.accessibility.core.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record AccessibilityRequest(
        Integer municipalityId,
        @NotNull Double searchRadiusInMeters,
        @NotNull Double startLocationLatitude,
        @NotNull Double startLocationLongitude,
        Double vehicleLengthInCm,
        Double vehicleHeightInCm,
        Double vehicleWidthInCm,
        Double vehicleWeightInKg,
        Double vehicleAxleLoadInKg,
        Set<TransportType> transportTypes,
        Set<TrafficSignType> trafficSignTypes) {

}
