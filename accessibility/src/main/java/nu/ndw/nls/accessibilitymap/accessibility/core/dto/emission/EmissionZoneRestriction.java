package nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.NonNull;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import org.springframework.validation.annotation.Validated;

@Validated
@Builder
@With
public record EmissionZoneRestriction(
        @NotNull String id,
        @NotNull Set<FuelType> fuelTypes,
        @NotNull Set<TransportType> transportTypes,
        Maximum vehicleWeightInKg) {

    public boolean isRelevant(
            Double vehicleWeightInKg,
            @NonNull Set<FuelType> relevantFuelTypes,
            @NonNull Set<TransportType> relevantTransportTypes) {

        List<Supplier<Boolean>> activeExemptions = new ArrayList<>();

        if (!fuelTypes().isEmpty() && !relevantFuelTypes.isEmpty()) {
            activeExemptions.add(() -> fuelTypes().stream().anyMatch(relevantFuelTypes::contains));
        }

        if (!transportTypes().isEmpty() && !relevantTransportTypes.isEmpty()) {
            activeExemptions.add(() -> transportTypes().stream().anyMatch(relevantTransportTypes::contains));
        }

        if (Objects.nonNull(vehicleWeightInKg()) && Objects.nonNull(vehicleWeightInKg)) {
            activeExemptions.add(() -> !vehicleWeightInKg().isExceeding(vehicleWeightInKg, false));
        }

        return activeExemptions.stream().allMatch(Supplier::get);
    }
}
