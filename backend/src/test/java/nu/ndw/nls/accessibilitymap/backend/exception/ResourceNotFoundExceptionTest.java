package nu.ndw.nls.accessibilitymap.backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor() {

        ResourceNotFoundException exception = new ResourceNotFoundException("message");

        assertThat(exception.getMessage()).isEqualTo("message");
    }
}