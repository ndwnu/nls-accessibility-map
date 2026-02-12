package nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility;

import static org.assertj.core.api.Assertions.assertThat;

import com.graphhopper.util.shapes.BBox;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.ZoneCodeType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestTest extends ValidationTest {

    private AccessibilityRequest accessibilityRequest;

    @BeforeEach
    void setUp() {

        accessibilityRequest = AccessibilityRequest.builder()
                .timestamp(OffsetDateTime.now())
                .maxSearchDistanceInMeters(1d)
                .startLocationLatitude(2d)
                .startLocationLongitude(3d)
                .requestArea(BBox.fromPoints(1.0, 2.0, 3.0, 4.0))
                .searchArea(BBox.fromPoints(10.0, 12.0, 13.0, 14.0))
                .build();
    }

    @Test
    void validate() {

        validate(accessibilityRequest, List.of(), List.of());
    }

    @Test
    void validate_timestamp_null() {

        accessibilityRequest = accessibilityRequest.withTimestamp(null);
        validate(accessibilityRequest, List.of("timestamp"), List.of("must not be null"));
    }

    @Test
    void validate_searchRadiusInMeters_null() {

        accessibilityRequest = accessibilityRequest.withMaxSearchDistanceInMeters(null);
        validate(accessibilityRequest, List.of("maxSearchDistanceInMeters"), List.of("must not be null"));
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
    void validate_requestArea_null() {

        accessibilityRequest = accessibilityRequest.withRequestArea(null);
        validate(accessibilityRequest, List.of("requestArea"), List.of("must not be null"));
    }

    @Test
    void validate_searchArea_null() {

        accessibilityRequest = accessibilityRequest.withSearchArea(null);
        validate(accessibilityRequest, List.of("searchArea"), List.of("must not be null"));
    }

    @Test
    void excludeTrafficSignTextSignTypes() {

        accessibilityRequest = accessibilityRequest.withExcludeTrafficSignTextSignTypes(Set.of(TextSignType.EMISSION_ZONE));
        assertThat(accessibilityRequest.excludeTrafficSignTextSignTypes()).containsExactlyInAnyOrder(TextSignType.EMISSION_ZONE);
    }

    @Test
    void excludeTrafficSignTextSignTypes_defaultValue() {

        accessibilityRequest = accessibilityRequest.withExcludeTrafficSignTextSignTypes(null);
        assertThat(accessibilityRequest.excludeTrafficSignTextSignTypes()).containsExactlyInAnyOrder(
                TextSignType.EXCLUDING,
                TextSignType.PRE_ANNOUNCEMENT);
    }

    @Test
    void excludeTrafficSignZoneCodeTypes() {

        accessibilityRequest = accessibilityRequest.withExcludeTrafficSignZoneCodeTypes(Set.of(ZoneCodeType.START));
        assertThat(accessibilityRequest.excludeTrafficSignZoneCodeTypes()).containsExactly(ZoneCodeType.START);
    }

    @Test
    void excludeTrafficSignZoneCodeTypes_defaultValue() {

        accessibilityRequest = accessibilityRequest.withExcludeTrafficSignZoneCodeTypes(null);
        assertThat(accessibilityRequest.excludeTrafficSignZoneCodeTypes()).containsExactly(ZoneCodeType.END);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true,
            false, true,
            true, false,
            false, false
            """)
    void excludeTrafficSignZoneCodeTypes(boolean latitude, boolean longitude) {

        accessibilityRequest = accessibilityRequest
                .withEndLocationLatitude(latitude ? 1D : null)
                .withEndLocationLongitude(longitude ? 2D : null);
        assertThat(accessibilityRequest.hasEndLocation()).isEqualTo(latitude && longitude);
    }

    @Test
    void getRequestAreaString() {

        assertThat(accessibilityRequest.getRequestAreaString()).contains("2.0,4.0,1.0,3.0");
    }

    @Test
    void getSearchAreaString() {

        assertThat(accessibilityRequest.getSearchAreaString()).contains("12.0,14.0,10.0,13.0");
    }

    @Override
    protected Class<?> getClassToTest() {

        return accessibilityRequest.getClass();
    }
}
