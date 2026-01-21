package nu.ndw.nls.accessibilitymap.backend.accessibility.api.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DecimalValueMapper {

    private static final int NEW_SCALE = 2;

    private DecimalValueMapper() {
    }

    public static BigDecimal mapToValue(Double value, int decimalPlaces) {
        return BigDecimal.valueOf(value).movePointLeft(decimalPlaces).setScale(NEW_SCALE, RoundingMode.HALF_UP);
    }
}
