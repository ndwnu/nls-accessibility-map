package nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.validation.annotation.Validated;

@Validated
public record EmissionZone(
        @NotNull @JsonProperty("trafficRegulationOrderId") String id,
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
