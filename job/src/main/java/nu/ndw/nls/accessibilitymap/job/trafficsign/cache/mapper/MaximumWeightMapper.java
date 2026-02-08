package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

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

        if (Objects.nonNull(vehicleCategoriesMaximumWeightInKg) && Objects.isNull(maximumWeightInKg)) {
            return vehicleCategoriesMaximumWeightInKg;
        }

        if (Objects.isNull(vehicleCategoriesMaximumWeightInKg)) {
            return Maximum.builder().value(maximumWeightInKg).build();
        }

        if (vehicleCategoriesMaximumWeightInKg.value() <= maximumWeightInKg) {
            return vehicleCategoriesMaximumWeightInKg;
        } else {
            return Maximum.builder().value(maximumWeightInKg).build();
        }
    }

    @SuppressWarnings("java:S109")
    public Maximum map(Set<VehicleCategory> vehicleCategories) {

        if (Objects.isNull(vehicleCategories)) {
            return null;
        }

        return vehicleCategories.stream()
                .map(vehicleCategory ->
                        switch (vehicleCategory) {
                            case M, N, M_1 -> null;
                            case M_2 -> 5_000D;
                            case N_1 -> 35_000D;
                            case N_2 -> 12_000D;
                            case M_3, N_3 -> Double.MAX_VALUE;
                            case UNKNOWN -> throw new IllegalStateException("Unknown vehicle category '%s'." .formatted(vehicleCategory));
                        })
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .map(minimalWeightRestriction -> Maximum.builder()
                        .value(minimalWeightRestriction)
                        .build())
                .orElse(null);
    }
}
