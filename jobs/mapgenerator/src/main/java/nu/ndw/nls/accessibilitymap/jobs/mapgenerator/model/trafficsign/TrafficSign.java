package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model.trafficsign;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model.Direction;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;

@Builder
public record TrafficSign(
        @NonNull Integer id,
        @NonNull Integer roadSectionId,
        @NonNull TrafficSignType trafficSignType,
        @NonNull Double latitude,
        @NonNull Double longitude,
        @NonNull Direction direction,
        @NonNull Double fraction,
        URI iconUri,
        @NonNull
        List<TextSign> textSigns
) {

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
