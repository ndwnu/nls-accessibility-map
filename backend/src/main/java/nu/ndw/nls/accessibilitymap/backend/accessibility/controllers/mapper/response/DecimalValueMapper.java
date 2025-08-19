package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DecimalValueMapper {

    private DecimalValueMapper() {
    }

    private static final int NEW_SCALE = 2;

    public static BigDecimal mapToValue(Double value, int decimalPlaces) {
        return BigDecimal.valueOf(value).movePointLeft(decimalPlaces).setScale(NEW_SCALE, RoundingMode.HALF_UP);
    }
}
