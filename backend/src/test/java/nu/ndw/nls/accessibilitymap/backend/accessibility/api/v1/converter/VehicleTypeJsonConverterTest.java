package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.converter;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleTypeJsonConverterTest {


    private VehicleTypeJsonConverter vehicleTypeJsonConverter;

    @BeforeEach
    void setUp() {

        vehicleTypeJsonConverter = new VehicleTypeJsonConverter();
    }

    @ParameterizedTest
    @EnumSource(VehicleTypeJson.class)
    void convert_null(VehicleTypeJson vehicleTypeJson) {

        assertThat(vehicleTypeJsonConverter.convert(vehicleTypeJson.getValue())).isEqualTo(vehicleTypeJson);
    }
}
