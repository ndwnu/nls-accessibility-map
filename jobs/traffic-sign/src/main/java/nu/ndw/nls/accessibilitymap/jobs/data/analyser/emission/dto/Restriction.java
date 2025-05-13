package nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Builder;
import lombok.With;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record Restriction(
        @NotNull String id,
        FuelType fuelType,
        VehicleType vehicleType,
        @JsonProperty("euVehicleCategories") Set<VehicleCategory> vehicleCategories) {

}
