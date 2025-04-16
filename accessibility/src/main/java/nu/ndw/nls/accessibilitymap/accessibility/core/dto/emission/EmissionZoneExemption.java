package nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClassification;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import org.springframework.validation.annotation.Validated;

@Validated
@Builder
public record EmissionZoneExemption(
        @NotNull OffsetDateTime startTime,
        @NotNull OffsetDateTime endTime,
        @NotEmpty Set<EmissionClassification> emissionClassifications,
        @NotEmpty Set<TransportType> transportTypes,
        @NotNull Maximum vehicleWeightInKg) {

}
