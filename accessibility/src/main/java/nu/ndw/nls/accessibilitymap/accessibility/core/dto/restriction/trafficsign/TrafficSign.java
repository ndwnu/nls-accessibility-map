package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import org.springframework.validation.annotation.Validated;

@Builder(toBuilder = true)
@With
@Validated
public record TrafficSign(
        @NotNull Integer id,
        @NotNull String externalId,
        @NotNull Integer roadSectionId,
        @NotNull TrafficSignType trafficSignType,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotNull Direction direction,
        @NotNull Double fraction,
        @NotNull Double networkSnappedLatitude,
        @NotNull Double networkSnappedLongitude,
        URI iconUri,
        ZoneCodeType zoneCodeType,
        String trafficRegulationOrderId,
        @NotNull TransportRestrictions transportRestrictions,
        @NotNull List<SupplementaryTrafficSign> supplementaryTrafficSigns) implements Restriction {

    @Override
    public boolean isRestrictive(AccessibilityRequest accessibilityRequest) {
        return TrafficSignExclusionCalculator.isNotExcluded(this, accessibilityRequest)
               && TrafficSignRestrictionCalculator.isRestrictive(this, accessibilityRequest);
    }

    @Override
    public @NonNull String toString() {
        return "TrafficSign(" +
               "id=" + id +
               ", externalId='" + externalId + "'" +
               ", roadSectionId=" + roadSectionId +
               ", trafficSignType=" + trafficSignType +
               ", fraction=" + fraction +
               ", networkSnappedLatitude=" + networkSnappedLatitude +
               ", networkSnappedLongitude=" + networkSnappedLongitude +
               ", direction=" + direction +
               ')';
    }
}
