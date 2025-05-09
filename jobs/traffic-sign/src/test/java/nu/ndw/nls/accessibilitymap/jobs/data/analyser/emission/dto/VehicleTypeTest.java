package nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleTypeTest {

    @ParameterizedTest
    @EnumSource(value = VehicleType.class)
    void fromValue(VehicleType vehicleType) {

        assertThat(VehicleType.fromValue(vehicleType.getValue())).isEqualTo(vehicleType);
    }

    @Test
    void fromValue_invalidValue() {

        assertThat(VehicleType.fromValue("invalidValue")).isEqualTo(VehicleType.UNKNOWN);
    }
}