package nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import org.springframework.validation.annotation.Validated;

@Validated
@Builder
public record EmissionZoneRestriction(
        @NotNull String id,
        @NotNull Set<FuelType> fuelTypes,
        @NotNull Set<TransportType> vehicleTypes,
        Maximum vehicleWeightInKg) {

}
