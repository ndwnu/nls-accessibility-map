package nu.ndw.nls.accessibilitymap.accessibility.core.dto.value;

import java.util.Objects;
import lombok.Builder;

@Builder
public record Maximum(Double value) {

    private static final Maximum NO_MAXIMUM = Maximum.builder().value(null).build();

    public boolean isExceeding(Double testValue, boolean inclusive) {

        if (Objects.isNull(value) || Objects.isNull(testValue)) {
            return false;
        }

        if (inclusive) {
            return testValue >= value;
        } else {
            return testValue > value;
        }
    }

    public static Maximum noMaximum() {
        return NO_MAXIMUM;
    }
}
