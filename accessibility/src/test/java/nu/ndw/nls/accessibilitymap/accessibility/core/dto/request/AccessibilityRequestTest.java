package nu.ndw.nls.accessibilitymap.accessibility.core.dto.request;

import java.util.List;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestTest extends ValidationTest {

    private AccessibilityRequest accessibilityRequest;

    @BeforeEach
    void setUp() {

        accessibilityRequest = AccessibilityRequest.builder()
                .searchRadiusInMeters(1d)
                .startLocationLatitude(2d)
                .startLocationLongitude(3d)
                .build();
    }

    @Test
    void validate() {

        validate(accessibilityRequest, List.of(), List.of());
    }

    @Test
    void validate_searchRadiusInMeters_null() {

        accessibilityRequest = accessibilityRequest.withSearchRadiusInMeters(null);
        validate(accessibilityRequest, List.of("searchRadiusInMeters"), List.of("must not be null"));
    }

    @Test
    void validate_startLocationLatitude_null() {

        accessibilityRequest = accessibilityRequest.withStartLocationLatitude(null);
        validate(accessibilityRequest, List.of("startLocationLatitude"), List.of("must not be null"));
    }

    @Test
    void validate_startLocationLongitude_null() {

        accessibilityRequest = accessibilityRequest.withStartLocationLongitude(null);
        validate(accessibilityRequest, List.of("startLocationLongitude"), List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {

        return accessibilityRequest.getClass();
    }
}
