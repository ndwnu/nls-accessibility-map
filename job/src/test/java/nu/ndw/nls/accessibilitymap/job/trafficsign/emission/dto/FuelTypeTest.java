package nu.ndw.nls.accessibilitymap.job.trafficsign.emission.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FuelTypeTest {

    @ParameterizedTest
    @EnumSource(value = FuelType.class)
    void fromValue(FuelType fuelType) {

        assertThat(FuelType.fromValue(fuelType.getValue())).isEqualTo(fuelType);
    }

    @Test
    void fromValue_invalidValue() {

        assertThat(FuelType.fromValue("invalidValue")).isEqualTo(FuelType.UNKNOWN);
    }
}
