package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionZoneTypeJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import org.springframework.validation.annotation.Validated;

@Builder
@Validated
public record AccessibilityRequest(
        String municipalityId,
        @NotNull Double endLatitude,
        @NotNull Double endLongitude,
        Double vehicleLengthInMeters,
        Double vehicleHeightInMeters,
        Double vehicleWidthInMeters,
        Double vehicleWeightInKg,
        Double vehicleAxleLoadInKg,
        List<FuelTypeJson> fuelTypes,
        List<String> excludeRestrictionsWithEmissionZoneIds,
        List<EmissionZoneTypeJson> excludeRestrictionsWithEmissionZoneTypes,
        EmissionClassJson emissionClass,
        VehicleTypeJson vehicleType,
        boolean vehicleHasTrailer) {

}