package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
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
        Double blackCode,
        @NotNull List<TextSign> textSigns,
        ZoneCodeType zoneCodeType,
        String trafficRegulationOrderId,
        @NotNull TransportRestrictions transportRestrictions) implements Restriction {

    public boolean hasTimeWindowedSign() {

        return textSigns
                .stream()
                .anyMatch(TextSign::hasWindowTime);
    }

    public Optional<TextSign> findFirstTimeWindowedSign() {

        return textSigns
                .stream()
                .filter(TextSign::hasWindowTime)
                .findFirst();
    }

    @Override
    public boolean isRestrictive(AccessibilityRequest accessibilityRequest) {
        return TrafficSignExclusionCalculator.isNotExcluded(this, accessibilityRequest)
               && TrafficSignRestrictionCalculator.isRestrictive(this, accessibilityRequest);
    }
}
