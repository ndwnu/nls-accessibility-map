package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.FuelTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FuelTypeJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

class FuelTypeMapperTest {

    private FuelTypeMapper fuelTypeMapper = new FuelTypeMapper();

    @BeforeEach
    void setUp() {
        fuelTypeMapper = new FuelTypeMapper();
    }

    @Test
    void mapFuelType_shouldThrowNotSupportedException() {
        FuelTypeJson mockFuelType = Mockito.mock(FuelTypeJson.class);
        when(mockFuelType.name()).thenReturn("unsupported fuel type");

        assertThatThrownBy(() -> fuelTypeMapper.mapFuelType(mockFuelType))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid fuel type: unsupported fuel type");
    }

    @Test
    void mapFuelType_shouldReturnNullWhenMappingNullFuelTypeJson() {

        FuelType result = fuelTypeMapper.mapFuelType(null);

        assertThat(result).isNull();
    }

    @Test
    void mapFuelType_shouldThrowInvalidFuelTypeException() {
        assertThatThrownBy(() -> fuelTypeMapper.mapFuelTypeJson(FuelType.UNKNOWN))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid fuel type: UNKNOWN");
    }

    @ParameterizedTest
    @EnumSource(FuelTypeJson.class)
    void mapFuelType_AllSupportedFuelTypeJsonValues(FuelTypeJson fuelTypeJson) {

        FuelType result = fuelTypeMapper.mapFuelType(fuelTypeJson);

        assertThat(result).isEqualTo((FuelType.valueOf(fuelTypeJson.name())));
    }
}
