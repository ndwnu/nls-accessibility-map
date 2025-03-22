package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.dto.value;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RangeTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
            10, 20, 20, true
            10, 20, 10, true
            10, 20, 9.9, false
            10, 20, 20.1, false
            10, , 10, true
            10, , 9.9, false
            , 10, 10.1, false
            , 10, 10, true
            , , 5, true
            """)
    void isWithinOrIsNotWithin_inclusive(Double min, Double max, Double testValue, boolean expectedResult) {

        Range range = Range.builder()
                .min(min)
                .max(max)
                .build();

        assertThat(range.isWithin(testValue, true)).isEqualTo(expectedResult);
        assertThat(range.isNotWithin(testValue, true)).isEqualTo(!expectedResult);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            10, 20, 20, false
            10, 20, 10, false
            10, 20, 10.1, true
            10, 20, 19.9, true
            10, , 10, false
            10, , 10.1, true
            , 10, 10, false
            , 10, 9.9, true
            , , 5, true
            """)
    void isWithinOrIsNotWithin_exclusive(Double min, Double max, Double testValue, boolean expectedResult) {

        Range range = Range.builder()
                .min(min)
                .max(max)
                .build();

        assertThat(range.isWithin(testValue, false)).isEqualTo(expectedResult);
        assertThat(range.isNotWithin(testValue, false)).isEqualTo(!expectedResult);
    }
}