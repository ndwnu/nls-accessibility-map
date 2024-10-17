package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DirectionTest {

    @Test
    void isForward_ok() {
        assertThat(Direction.FORWARD.isForward()).isTrue();
        assertThat(Direction.BACKWARD.isForward()).isFalse();
    }

    @Test
    void isBackward_ok() {
        assertThat(Direction.FORWARD.isBackward()).isFalse();
        assertThat(Direction.BACKWARD.isBackward()).isTrue();
    }
}