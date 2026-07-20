package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category.LOCAL_TRAFFIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CategoryTest {

    @Test
    void getValue() {
        assertThat(LOCAL_TRAFFIC.getValue()).isEqualTo("LocalTraffic");
    }

    @Test
    void fromValue() {
        assertThat(Category.fromValue("LocalTraffic")).isEqualTo(LOCAL_TRAFFIC);
    }

    @Test
    void fromValue_unknown_throwsException() {
        assertThatThrownBy(() -> Category.fromValue("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unexpected value 'unknown'");
    }
}