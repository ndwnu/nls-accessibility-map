package nu.ndw.nls.accessibilitymap.backend.yaml.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvalidDataExceptionTest {

    @Test
    void constructor() {

        InvalidDataException exception = new InvalidDataException(List.of("error1", "error2"));

        assertThat(exception.getMessage()).isEqualTo("error1, error2");
    }
}