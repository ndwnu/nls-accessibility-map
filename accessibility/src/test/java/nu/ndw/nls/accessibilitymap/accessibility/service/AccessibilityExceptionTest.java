package nu.ndw.nls.accessibilitymap.accessibility.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AccessibilityExceptionTest {

    @Test
    void constructor() {

        AccessibilityException accessibilityException = new AccessibilityException("message");
        assertThat(accessibilityException.getMessage()).isEqualTo("message");
    }
}
