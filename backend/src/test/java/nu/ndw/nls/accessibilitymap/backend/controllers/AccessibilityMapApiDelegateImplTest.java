package nu.ndw.nls.accessibilitymap.backend.controllers;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
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
        ResponseEntity<List<Integer>> response = accessibilityMapApiDelegate.getInaccessibleRoadSections(344, VehicleTypeJson.CAR
                , null, null, null, null, null, false);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
