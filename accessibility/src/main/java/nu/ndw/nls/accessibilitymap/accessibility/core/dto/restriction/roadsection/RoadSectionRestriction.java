package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.roadsection;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record RoadSectionRestriction(
        @NotNull Integer id,
        @NotNull Direction direction,
        @NotNull Double fraction,
        @NotNull Double networkSnappedLatitude,
        @NotNull Double networkSnappedLongitude
) implements Restriction {

    @Override
    public boolean isRestrictive(AccessibilityRequest accessibilityRequest) {
        return true;
    }

    @Override
    public Integer roadSectionId() {
        return id();
    }

    @Override
    public @NonNull String toString() {
        return "RoadSectionRestriction(" +
               "id=" + id +
               ", direction=" + direction +
               ", fraction=" + fraction +
               ", networkSnappedLatitude=" + networkSnappedLatitude +
               ", networkSnappedLongitude=" + networkSnappedLongitude +
               ')';
    }
}
