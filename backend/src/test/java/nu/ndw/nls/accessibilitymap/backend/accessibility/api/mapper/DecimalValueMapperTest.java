package nu.ndw.nls.accessibilitymap.backend.accessibility.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DecimalValueMapperTest {

    @Test
    void map_ok() {
        Double inputValue = 1234.56;
        int decimalPlaces = 2;
        BigDecimal expected = new BigDecimal("12.35");

        BigDecimal result = DecimalValueMapper.mapToValue(inputValue, decimalPlaces);

        assertEquals(expected, result);
    }
}
