package nu.ndw.nls.accessibilitymap.accessibility.core.dto.value;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MaximumTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
            20, 19.9, false
            20, 20, true
            20, , false
            , 20, false
            , , false
            """)
    void isExceeding_inclusive(
            Double value,
            Double testValue,
            boolean expectedResult) {

        Maximum maximum = Maximum.builder().value(value).build();

        assertThat(maximum.isExceeding(testValue, true)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            20, 20, false
            20, 20.1, true
            20, , false
            , 20.1, false
            , , false
            """)
    void isExceeding_exclusive(
            Double value,
            Double testValue,
            boolean expectedResult) {

        Maximum maximum = Maximum.builder().value(value).build();

        assertThat(maximum.isExceeding(testValue, false)).isEqualTo(expectedResult);
    }
}
