package nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import org.springframework.validation.annotation.Validated;

@Validated
@Builder
@With
public record EmissionZone(
        @NotNull String id,
        @NotNull EmissionZoneType type,
        @NotNull OffsetDateTime startTime,
        @NotNull OffsetDateTime endTime,
        @NotNull @Valid Set<EmissionZoneExemption> exemptions,
        @NotNull @Valid EmissionZoneRestriction restriction) {

    public boolean isActive(OffsetDateTime time) {

        return (time.isEqual(startTime) || time.isAfter(startTime))
                && time.isBefore(endTime);
    }

    public boolean isRelevant(
            Double vehicleWeightInKg,
            @NonNull Set<FuelType> fuelTypes,
            @NonNull Set<TransportType> transportTypes) {

        return restriction.isRelevant(vehicleWeightInKg, fuelTypes, transportTypes);
    }

    public boolean isExempt(
            @NonNull OffsetDateTime timestamp,
            Double vehicleWeightInKg,
            @NonNull Set<EmissionClass> emissionClasses,
            @NonNull Set<TransportType> transportTypes) {

        return exemptions.stream()
                .filter(exemption -> exemption.isActive(timestamp))
                .anyMatch(emissionZoneExemption ->
                        emissionZoneExemption.isExempt(vehicleWeightInKg, emissionClasses, transportTypes));
    }
}
