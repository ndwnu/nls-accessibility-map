package nu.ndw.nls.accessibilitymap.backend.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MapTypeJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AccessibilityMapApiDelegateImplTest {

    @InjectMocks
    private AccessibilityMapApiDelegateImpl accessibilityMapApiDelegate;

    @Test
    void testEndpoint_ok() {
        ResponseEntity<String> response = accessibilityMapApiDelegate.testEndpoint(MapTypeJson.nwb);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
