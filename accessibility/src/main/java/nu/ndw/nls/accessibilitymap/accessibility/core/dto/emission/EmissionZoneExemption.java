package nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.NonNull;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import org.springframework.validation.annotation.Validated;

@Validated
@Builder
@With
public record EmissionZoneExemption(
        @NotNull OffsetDateTime startTime,
        @NotNull OffsetDateTime endTime,
        @NotEmpty Set<EmissionClass> emissionClasses,
        @NotEmpty Set<TransportType> transportTypes,
        @NotNull Maximum vehicleWeightInKg) {

    public boolean isActive(OffsetDateTime time) {

        return (time.isEqual(startTime) || time.isAfter(startTime))
                && time.isBefore(endTime);
    }

    public boolean isExempt(
            Double vehicleWeightInKg,
            @NonNull Set<EmissionClass> emissionClasses,
            @NonNull Set<TransportType> transportTypes) {

        List<Supplier<Boolean>> activeExemptions = new ArrayList<>();

        if (!emissionClasses.isEmpty()) {
            activeExemptions.add(() -> emissionClasses().stream().anyMatch(emissionClasses::contains));
        }

        if (!transportTypes.isEmpty()) {
            activeExemptions.add(() -> transportTypes().stream().anyMatch(transportTypes::contains));
        }

        if (Objects.nonNull(vehicleWeightInKg)) {
            activeExemptions.add(() -> !vehicleWeightInKg().isExceeding(vehicleWeightInKg, false));
        }

        return activeExemptions.stream().allMatch(Supplier::get);
    }
}
