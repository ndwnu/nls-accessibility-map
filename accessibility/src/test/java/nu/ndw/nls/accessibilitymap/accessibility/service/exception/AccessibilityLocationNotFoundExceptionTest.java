package nu.ndw.nls.accessibilitymap.accessibility.service.exception;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityLocationNotFoundExceptionTest {

    @Test
    void constructor() {

        var accessibilityLocationNotFoundException = new AccessibilityLocationNotFoundException(new Location(1, 2, null));

        assertThat(accessibilityLocationNotFoundException.getMessage())
                .isEqualTo("Location could not be resolved at 1.0, 2.0. Please try a different location that is "
                           + "closer to actual road sections in the network.");
    }
}
