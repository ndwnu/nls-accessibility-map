package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.converter;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.FuelTypeJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FuelTypeJsonConverterTest {

    private FuelTypeJsonConverter fuelTypeJsonConverter;

    @BeforeEach
    void setUp() {

        fuelTypeJsonConverter = new FuelTypeJsonConverter();
    }

    @ParameterizedTest
    @EnumSource(FuelTypeJson.class)
    void convert_null(FuelTypeJson fuelTypeJson) {

        assertThat(fuelTypeJsonConverter.convert(fuelTypeJson.getValue())).isEqualTo(fuelTypeJson);
    }
}
