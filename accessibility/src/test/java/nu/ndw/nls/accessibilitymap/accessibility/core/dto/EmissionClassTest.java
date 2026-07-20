package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EmissionClassTest {

    @ParameterizedTest
    @EnumSource(value = EmissionClass.class)
    void getValue(EmissionClass emissionClass) {
        assertThat(emissionClass.getValue()).isEqualTo(expectedValue(emissionClass));
    }

    @ParameterizedTest
    @EnumSource(value = EmissionClass.class)
    void fromValue(EmissionClass emissionClass) {
        assertThat(EmissionClass.fromValue(emissionClass.getValue())).isEqualTo(emissionClass);
    }

    @Test
    void fromValue_unknown_throwsException() {
        assertThatThrownBy(() -> EmissionClass.fromValue("unknown-value"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unexpected value 'unknown-value'");
    }

    private static String expectedValue(EmissionClass emissionClass) {
        return switch (emissionClass) {
            case EURO_1 -> "Euro1";
            case EURO_2 -> "Euro2";
            case EURO_3 -> "Euro3";
            case EURO_4 -> "Euro4";
            case EURO_5 -> "Euro5";
            case EURO_6 -> "Euro6";
            case UNKNOWN -> "Unknown";
        };
    }
}
