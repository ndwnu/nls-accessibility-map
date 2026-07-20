package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DirectionTest {

    @Test
    void isForward() {
        assertThat(Direction.FORWARD.isForward()).isTrue();
        assertThat(Direction.BACKWARD.isForward()).isFalse();
    }

    @Test
    void isBackward() {
        assertThat(Direction.FORWARD.isBackward()).isFalse();
        assertThat(Direction.BACKWARD.isBackward()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Direction.class)
    void getValue(Direction direction) {
        assertThat(direction.getValue()).isEqualTo(expectedValue(direction));
    }

    @ParameterizedTest
    @EnumSource(value = Direction.class)
    void fromValue(Direction direction) {
        assertThat(Direction.fromValue(direction.getValue())).isEqualTo(direction);
    }

    @Test
    void fromValue_unknown_throwsException() {
        assertThatThrownBy(() -> Direction.fromValue("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unexpected value 'unknown'");
    }

    private static String expectedValue(Direction direction) {
        return switch (direction) {
            case FORWARD -> "Forward";
            case BACKWARD -> "Backward";
        };
    }
}
