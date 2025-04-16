package nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Set;
import org.springframework.validation.annotation.Validated;

@Validated
public record Exemption(
        @NotNull String id,
        @JsonProperty("overallStartTime") OffsetDateTime startTime,
        @JsonProperty("overallEndTime") OffsetDateTime endTime,
        @NotEmpty @JsonProperty("emissionClassificationEuros") Set<EuroClassification> euroClassifications,
        @NotEmpty @JsonProperty("euVehicleCategories") Set<VehicleCategory> vehicleCategories,
        @JsonProperty("maximalVehicleWeightAllowed") Integer vehicleWeightInKg) {

}
