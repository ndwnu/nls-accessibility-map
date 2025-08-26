package nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import org.springframework.validation.annotation.Validated;

@Builder
@Validated
public record AccessibilityRequest(
        String municipalityId,
        @NotNull Double startLatitude,
        @NotNull Double startLongitude,
        Double vehicleLengthInMeters,
        Double vehicleHeightInMeters,
        Double vehicleWidthInMeters,
        Double vehicleWeightInKg,
        Double vehicleAxleLoadInKg,
        FuelTypeJson fuelType,
        EmissionClassJson emissionClass,
        VehicleTypeJson vehicleType,
        boolean vehicleHasTrailer) {

}