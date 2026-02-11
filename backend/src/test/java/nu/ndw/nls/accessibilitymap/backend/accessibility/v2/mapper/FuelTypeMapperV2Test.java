package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FuelTypeJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mockito;

class FuelTypeMapperV2Test {

    private FuelTypeMapperV2 fuelTypeMapperV2;

    @BeforeEach
    void setUp() {
        fuelTypeMapperV2 = new FuelTypeMapperV2();
    }

    @ParameterizedTest
    @EnumSource(value = FuelTypeJson.class)
    void map(FuelTypeJson fuelTypeJson) {

        FuelType result = fuelTypeMapperV2.map(fuelTypeJson);

        assertThat(result).isEqualTo((FuelType.valueOf(fuelTypeJson.name())));
    }

    @Test
    void map_fuelTypeJson_unsupportedValue() {
        FuelTypeJson mockFuelType = Mockito.mock(FuelTypeJson.class);
        when(mockFuelType.name()).thenReturn("unsupported fuel type");

        assertThatThrownBy(() -> fuelTypeMapperV2.map(mockFuelType))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid fuel type: unsupported fuel type");
    }

    @ParameterizedTest
    @NullSource
    void map_shouldReturnNullWhenMappingNullFuelTypeJson(FuelTypeJson fuelTypeJson) {

        FuelType result = fuelTypeMapperV2.map(fuelTypeJson);

        assertThat(result).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = FuelType.class, mode = Mode.EXCLUDE, names = "UNKNOWN")
    void map(FuelType fuelType) {

        FuelTypeJson result = fuelTypeMapperV2.map(fuelType);

        assertThat(result).isEqualTo((FuelTypeJson.valueOf(fuelType.name())));
    }

    @ParameterizedTest
    @EnumSource(value = FuelType.class, mode = Mode.INCLUDE, names = "UNKNOWN")
    void map_AllUnsupportedFuelTypeValues(FuelType fuelType) {

        assertThatThrownBy(() -> fuelTypeMapperV2.map(fuelType))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid fuel type: UNKNOWN");
    }
}
