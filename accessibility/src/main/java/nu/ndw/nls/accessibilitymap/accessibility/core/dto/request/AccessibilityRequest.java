package nu.ndw.nls.accessibilitymap.accessibility.core.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
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
        List<TransportType> transportTypes) {

}
