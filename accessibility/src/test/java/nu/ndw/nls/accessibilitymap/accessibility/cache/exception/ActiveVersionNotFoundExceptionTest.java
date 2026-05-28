package nu.ndw.nls.accessibilitymap.accessibility.cache.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class ActiveVersionNotFoundExceptionTest {

    @Test
    void constructor() {

        ActiveVersionNotFoundException activeVersionNotFoundException = new ActiveVersionNotFoundException("cacheName");
        assertThat(activeVersionNotFoundException.getMessage()).isEqualTo("No active version found for cache cacheName");
    }
}
