package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FuelTypeTest {

    @ParameterizedTest
    @EnumSource(value = FuelType.class)
    void getType(FuelType fuelType) {
        assertThat(fuelType.getType()).isEqualTo(expectedType(fuelType));
    }

    @ParameterizedTest
    @EnumSource(value = FuelType.class)
    void fromValue(FuelType fuelType) {
        assertThat(FuelType.fromValue(fuelType.getType())).isEqualTo(fuelType);
    }

    @Test
    void fromValue_unknown_throwsException() {
        assertThatThrownBy(() -> FuelType.fromValue("unknown-value"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unexpected value 'unknown-value'");
    }

    private static String expectedType(FuelType fuelType) {
        return switch (fuelType) {
            case COMPRESSED_NATURAL_GAS -> "CompressedNaturalGas";
            case DIESEL -> "Diesel";
            case ETHANOL -> "Ethanol";
            case ELECTRIC -> "Electric";
            case HYDROGEN -> "Hydrogen";
            case LIQUEFIED_PETROLEUM_GAS -> "LiquefiedPetroleumGas";
            case LIQUEFIED_NATURAL_GAS -> "LiquefiedNaturalGas";
            case PETROL -> "Petrol";
            case UNKNOWN -> "Unknown";
        };
    }
}
