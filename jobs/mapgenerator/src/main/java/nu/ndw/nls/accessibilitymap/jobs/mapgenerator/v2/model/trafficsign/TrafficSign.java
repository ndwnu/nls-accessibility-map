package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;

@Builder
public record TrafficSign(
        @NonNull Integer roadSectionId,
        @NonNull TrafficSignType trafficSignType,
        @NonNull Double latitude,
        @NonNull Double longitude,
        @NonNull TrafficSignDirection direction,
        @NonNull Double fraction,
        URI iconUri,
        @NonNull
        List<TextSign> textSigns
) {

    public boolean hasWindowTime() {
        return textSigns
                .stream()
                .anyMatch(TextSign::hasWindowTime);
    }

    public boolean hasNoExcludingOrPreAnnouncement() {
        return textSigns
                .stream()
                .anyMatch(TextSign::hasNoExcludingOrPreAnnouncement);
    }

    public Optional<TextSign> findFirstWindowTimeTextSign() {
        return textSigns
                .stream()
                .filter(TextSign::hasWindowTime)
                .findFirst();
    }
}
