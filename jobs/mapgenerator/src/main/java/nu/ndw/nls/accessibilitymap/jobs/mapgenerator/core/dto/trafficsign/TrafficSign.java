package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.Direction;
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
        @NotNull List<TextSign> textSigns) {

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
}
