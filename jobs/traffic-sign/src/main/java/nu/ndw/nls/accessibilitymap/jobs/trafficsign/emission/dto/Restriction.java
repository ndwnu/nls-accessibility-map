package nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import org.springframework.validation.annotation.Validated;

@Validated
public record Restriction(
        @NotNull String id,
        @NotNull FuelType fuelType,
        @NotNull VehicleType vehicleType,
        @NotNull @JsonProperty("euVehicleCategories") Set<VehicleCategory> vehicleCategories) {

}
