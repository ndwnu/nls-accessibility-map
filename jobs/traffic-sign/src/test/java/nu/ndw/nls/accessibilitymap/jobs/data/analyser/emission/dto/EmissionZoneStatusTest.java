package nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionZoneStatusTest {

    @ParameterizedTest
    @EnumSource(value = EmissionZoneStatus.class)
    void fromValue(EmissionZoneStatus emissionZoneStatus) {

        assertThat(EmissionZoneStatus.fromValue(emissionZoneStatus.getValue())).isEqualTo(emissionZoneStatus);
    }

    @Test
    void fromValue_invalidValue() {

        assertThat(EmissionZoneStatus.fromValue("invalidValue")).isEqualTo(EmissionZoneStatus.UNKNOWN);
    }
}