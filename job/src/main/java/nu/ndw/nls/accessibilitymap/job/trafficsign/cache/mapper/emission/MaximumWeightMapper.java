package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.emission;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.job.trafficsign.emission.dto.VehicleCategory;
import org.springframework.stereotype.Component;

@Component
public class MaximumWeightMapper {

    @SuppressWarnings("java:S1142")
    public Maximum map(Set<VehicleCategory> vehicleCategories, Double maximumWeightInKg) {

        Maximum vehicleCategoriesMaximumWeightInKg = map(vehicleCategories);
        if (Objects.isNull(vehicleCategoriesMaximumWeightInKg) && Objects.isNull(maximumWeightInKg)) {
            return null;
        }

        if (Objects.nonNull(vehicleCategoriesMaximumWeightInKg)
            && Objects.nonNull(maximumWeightInKg)) {
            return vehicleCategoriesMaximumWeightInKg.isExceeding(maximumWeightInKg, false)
                    ? vehicleCategoriesMaximumWeightInKg
                    : Maximum.builder().value(maximumWeightInKg).build();
        }

        if (Objects.nonNull(vehicleCategoriesMaximumWeightInKg)) {
            return vehicleCategoriesMaximumWeightInKg;
        }

        return Maximum.builder().value(maximumWeightInKg).build();
    }

    public Maximum map(Set<VehicleCategory> vehicleCategories) {

        if (Objects.isNull(vehicleCategories)) {
            return null;
        }

        List<Double> maximumWeightsInKg = vehicleCategories.stream()
                .map(this::mapMaximumWeightInKg)
                .toList();

        // A vehicle in any of the categories is covered, so a single category without an upper limit means the whole set has none.
        if (maximumWeightsInKg.isEmpty() || maximumWeightsInKg.stream().anyMatch(Objects::isNull)) {
            return null;
        }

        return Maximum.builder()
                .value(Collections.max(maximumWeightsInKg))
                .build();
    }

    @SuppressWarnings("java:S109")
    private Double mapMaximumWeightInKg(VehicleCategory vehicleCategory) {

        return switch (vehicleCategory) {
            case M, M_1, M_2, M_3, N, N_2, N_3 -> null;
            case N_1 -> 3_500D;
            case UNKNOWN -> throw new IllegalStateException("Unknown vehicle category '%s'.".formatted(vehicleCategory));
        };
    }
}
