package nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EmissionZoneTypeTest {

    @ParameterizedTest
    @EnumSource(value = EmissionZoneType.class)
    void getValue(EmissionZoneType emissionZoneType) {
        assertThat(emissionZoneType.getValue()).isEqualTo(expectedValue(emissionZoneType));
    }

    @ParameterizedTest
    @EnumSource(value = EmissionZoneType.class)
    void fromValue(EmissionZoneType emissionZoneType) {
        assertThat(EmissionZoneType.fromValue(emissionZoneType.getValue())).isEqualTo(emissionZoneType);
    }

    @Test
    void fromValue_unknown_throwsException() {
        assertThatThrownBy(() -> EmissionZoneType.fromValue("unknown-value"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unexpected value 'unknown-value'");
    }

    private static String expectedValue(EmissionZoneType emissionZoneType) {
        return switch (emissionZoneType) {
            case ZERO -> "ZeroEmissionZone";
            case LOW -> "LowEmissionZone";
            case UNKNOWN -> "Unknown";
        };
    }
}
