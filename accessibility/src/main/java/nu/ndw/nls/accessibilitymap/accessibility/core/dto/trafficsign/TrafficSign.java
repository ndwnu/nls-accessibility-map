package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import org.springframework.validation.annotation.Validated;

@Builder
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
        URI iconUri,
        Double blackCode,
        @NotNull List<TextSign> textSigns,
        URI trafficSignOrderUrl,
        Restrictions restrictions) {

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

    public boolean isRelevant(AccessibilityRequest accessibilityRequest) {

        return hasRelevantTrafficSignOrContinue(accessibilityRequest)
                && hasRelevantRestrictionsOrContinue(accessibilityRequest);
    }

    private boolean hasRelevantRestrictionsOrContinue(AccessibilityRequest accessibilityRequest) {
        if (!restrictions.hasActiveRestrictions(accessibilityRequest)) {
            return true; // continue
        }

        return restrictions.isRestrictive(accessibilityRequest);
    }

    private boolean hasRelevantTrafficSignOrContinue(AccessibilityRequest accessibilityRequest) {
        if (Objects.isNull(trafficSignType) || Objects.isNull(accessibilityRequest.trafficSignTypes())) {
            return true; // continue
        }

        return accessibilityRequest.trafficSignTypes().contains(trafficSignType);
    }
}
