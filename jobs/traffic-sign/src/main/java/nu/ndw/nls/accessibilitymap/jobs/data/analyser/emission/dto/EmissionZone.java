package nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.With;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record EmissionZone(
        @NotNull String id,
        @NotNull String trafficRegulationOrderId,
        @NotNull @JsonProperty("emissionZoneType") EmissionZoneType type,
        @NotNull OffsetDateTime startTime,
        OffsetDateTime endTime,
        @NotNull @JsonProperty("eventStatus") EmissionZoneStatus status,
        @NotNull @JsonProperty("euVehicleCategoryAndEmissionClassificationRestrictionExemptions") List<Exemption> exemptions,
        @NotNull @JsonProperty("genericVehicleRestriction") Restriction restriction) {

    public boolean isActive() {
        return status == EmissionZoneStatus.ACTIVE;
    }
}
