package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto;

import java.util.List;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;

@Builder
public record VehicleArguments(
        VehicleTypeJson vehicleType,
        Float vehicleLength,
        Float vehicleWidth,
        Float vehicleHeight,
        Float vehicleWeight,
        Float vehicleAxleLoad,
        Boolean vehicleHasTrailer,
        EmissionClassJson emissionClass,
        List<FuelTypeJson> fuelTypes) {

}
