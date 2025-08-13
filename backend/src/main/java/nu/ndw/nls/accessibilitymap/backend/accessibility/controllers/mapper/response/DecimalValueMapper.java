package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DecimalValueMapper {

    public static BigDecimal mapToValue(Double value, int decimalPlaces) {
        return BigDecimal.valueOf(value)
                .movePointLeft(decimalPlaces)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
