package nu.ndw.nls.accessibilitymap.accessibility.nwb.messaging.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DrivingDirectionTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
            FORTH,true
            BOTH,true,
            BACK,false
            """)
    void isForwardAccessible(DrivingDirection drivingDirection, boolean expectedResult) {
        assertThat(drivingDirection.isForwardAccessible()).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            FORTH,false
            BOTH,true,
            BACK,true
            """)
    void isBackwardAccessible(DrivingDirection drivingDirection, boolean expectedResult) {
        assertThat(drivingDirection.isBackwardAccessible()).isEqualTo(expectedResult);
    }
}
