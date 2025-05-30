package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

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
}
