package nu.ndw.nls.accessibilitymap.job.trafficsign.emission.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehiclCategoryTest {

    @ParameterizedTest
    @EnumSource(value = VehicleCategory.class)
    void fromValue(VehicleCategory vehicleCategory) {

        assertThat(VehicleCategory.fromValue(vehicleCategory.getValue())).isEqualTo(vehicleCategory);
    }

    @Test
    void fromValue_invalidValue() {

        assertThat(VehicleCategory.fromValue("invalidValue")).isEqualTo(VehicleCategory.UNKNOWN);
    }
}
