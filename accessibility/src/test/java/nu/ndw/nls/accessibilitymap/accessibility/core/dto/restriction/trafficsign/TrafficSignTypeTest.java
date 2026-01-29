package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TrafficSignTypeTest {

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void fromRvvCode(TrafficSignType trafficSignType) {
        assertThat(TrafficSignType
                .fromRvvCode(trafficSignType.getRvvCode())).isEqualTo(trafficSignType);
    }

    @Test
    void fromRvvCode_exception() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> TrafficSignType.fromRvvCode("test"))
                .withMessage("Invalid TrafficSignType: test");
    }

}
