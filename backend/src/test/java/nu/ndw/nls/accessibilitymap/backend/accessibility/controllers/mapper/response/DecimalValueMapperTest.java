package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import static org.junit.jupiter.api.Assertions.*;

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