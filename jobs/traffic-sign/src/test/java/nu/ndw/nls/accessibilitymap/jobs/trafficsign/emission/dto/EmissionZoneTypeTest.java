package nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionZoneTypeTest {

    @ParameterizedTest
    @EnumSource(value = EmissionZoneType.class)
    void fromValue(EmissionZoneType emissionZoneType) {

        assertThat(EmissionZoneType.fromValue(emissionZoneType.getValue())).isEqualTo(emissionZoneType);
    }

    @Test
    void fromValue_invalidValue() {

        assertThat(EmissionZoneType.fromValue("invalidValue")).isEqualTo(EmissionZoneType.UNKNOWN);
    }
}