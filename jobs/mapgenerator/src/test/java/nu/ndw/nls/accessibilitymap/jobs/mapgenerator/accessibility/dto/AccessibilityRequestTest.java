package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.unit.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccessibilityRequestTest extends ValidationTest {

    private AccessibilityRequest accessibilityRequest;

    @BeforeEach
    void setUp() {

        accessibilityRequest = AccessibilityRequest.builder()
                .searchDistanceInMetres(1d)
                .trafficSignType(TrafficSignType.C7)
                .startLocationLatitude(2d)
                .startLocationLongitude(3d)
                .build();
    }

    @Test
    void validate_ok() {

        validate(accessibilityRequest, List.of(), List.of());
    }

    @Test
    void validate_searchDistanceInMetres_null() {

        accessibilityRequest = accessibilityRequest.withSearchDistanceInMetres(null);
        validate(accessibilityRequest, List.of("searchDistanceInMetres"), List.of("must not be null"));
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
    void validate_trafficSignType_null() {

        accessibilityRequest = accessibilityRequest.withTrafficSignType(null);
        validate(accessibilityRequest, List.of("trafficSignType"), List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {

        return accessibilityRequest.getClass();
    }
}