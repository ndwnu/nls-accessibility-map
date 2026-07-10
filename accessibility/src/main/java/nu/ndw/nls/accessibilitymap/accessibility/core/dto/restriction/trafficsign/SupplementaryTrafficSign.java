package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SupplementaryTrafficSign(@NotNull SupplementarySignType type, String text) {

    public boolean hasWindowTime() {
        return type.isWindowTime();
    }
}
