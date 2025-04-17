package nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.Builder;
import org.springframework.validation.annotation.Validated;

@Validated
@Builder
public record EmissionZone(
        @NotNull OffsetDateTime startTime,
        @NotNull OffsetDateTime endTime,
        @NotNull @Valid Set<EmissionZoneExemption> exemptions,
        @NotNull @Valid EmissionZoneRestriction restriction) {

    public boolean isActive(OffsetDateTime time) {
        return (startTime.isEqual(time) || startTime.isAfter(time))
                && (endTime.isEqual(time) || endTime.isBefore(time));
    }
}
