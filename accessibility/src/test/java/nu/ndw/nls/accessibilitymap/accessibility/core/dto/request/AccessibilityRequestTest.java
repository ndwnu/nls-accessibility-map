package nu.ndw.nls.accessibilitymap.accessibility.core.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.ZoneCodeType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
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

    @Test
    void excludeTextSignTypes() {

        accessibilityRequest = accessibilityRequest.withExcludeTrafficSignTextSignTypes(Set.of(TextSignType.EMISSION_ZONE));
        assertThat(accessibilityRequest.excludeTrafficSignTextSignTypes()).containsExactlyInAnyOrder(TextSignType.EMISSION_ZONE);
    }

    @Test
    void excludeTextSignTypes_defaultValue() {

        accessibilityRequest = accessibilityRequest.withExcludeTrafficSignTextSignTypes(null);
        assertThat(accessibilityRequest.excludeTrafficSignTextSignTypes()).containsExactlyInAnyOrder(
                TextSignType.EXCLUDING,
                TextSignType.PRE_ANNOUNCEMENT,
                TextSignType.FREE_TEXT);
    }

    @Test
    void excludeZoneCodeTypes() {

        accessibilityRequest = accessibilityRequest.withExcludeTrafficSignZoneCodeTypes(Set.of(ZoneCodeType.START));
        assertThat(accessibilityRequest.excludeTrafficSignZoneCodeTypes()).containsExactly(ZoneCodeType.START);
    }

    @Test
    void excludeZoneCodeTypes_defaultValue() {

        accessibilityRequest = accessibilityRequest.withExcludeTrafficSignZoneCodeTypes(null);
        assertThat(accessibilityRequest.excludeTrafficSignZoneCodeTypes()).containsExactly(ZoneCodeType.END);
    }

    @Override
    protected Class<?> getClassToTest() {

        return accessibilityRequest.getClass();
    }
}
