package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.dto.value;

import java.util.Objects;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record Range(
        Double min,
        Double max) {

    public boolean isNotWithin(Double value, boolean inclusive) {

        return !isWithin(value, inclusive);
    }

    @SuppressWarnings("java:S1142")
    public boolean isWithin(@NonNull Double value, boolean inclusive) {

        if ((Objects.isNull(min) && Objects.isNull(max))) {
            return true;
        }

        if (Objects.isNull(min)) {
            if (inclusive) {
                return value <= max;
            } else {
                return value < max;
            }
        }

        if (Objects.isNull(max)) {
            if (inclusive) {
                return value >= min;
            } else {
                return value > min;
            }
        }

        if (inclusive) {
            return value >= min && value <= max;
        } else {
            return value > min && value < max;
        }
    }
}
