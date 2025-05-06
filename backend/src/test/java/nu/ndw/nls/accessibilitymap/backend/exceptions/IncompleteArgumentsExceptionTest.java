package nu.ndw.nls.accessibilitymap.backend.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IncompleteArgumentsExceptionTest {

    @Test
    void constructor() {

        IncompleteArgumentsException exception = new IncompleteArgumentsException("message");

        assertThat(exception.getMessage()).isEqualTo("message");
    }
}