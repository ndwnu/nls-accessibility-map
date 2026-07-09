package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import lombok.Builder;

@Builder
public record SupplementaryTrafficSign(SupplementarySignType type, String text) {

    public boolean hasWindowTime() {
        return type() == SupplementarySignType.OB254 || type() == SupplementarySignType.OB256 || type() == SupplementarySignType.OB259;
    }
}
