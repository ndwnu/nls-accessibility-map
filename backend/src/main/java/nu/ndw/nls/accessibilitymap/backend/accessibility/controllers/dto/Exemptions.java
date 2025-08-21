package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record Exemptions(
        @NotNull EmissionZoneExemption emissionZone) {

}
