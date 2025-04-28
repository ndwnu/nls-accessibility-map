package nu.ndw.nls.accessibilitymap.backend.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MunicipalityNotFoundExceptionTest {

    @Test
    void constructor() {

        MunicipalityNotFoundException exception = new MunicipalityNotFoundException("message");

        assertThat(exception.getMessage()).isEqualTo("message");
    }
}