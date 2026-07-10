package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignTest extends ValidationTest {

    private TrafficSign trafficSign;

    @Mock
    private TransportRestrictions transportRestrictions;

    @BeforeEach
    void setUp() {
        trafficSign = TrafficSign.builder()
                .id(1)
                .externalId("id2")
                .roadSectionId(3)
                .fraction(0.2)
                .latitude(10.0)
                .longitude(20.0)
                .networkSnappedLatitude(2.0)
                .networkSnappedLongitude(3.0)
                .direction(Direction.BACKWARD)
                .supplementaryTrafficSigns(Collections.emptyList())
                .trafficSignType(TrafficSignType.C7)
                .transportRestrictions(transportRestrictions)
                .build();
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, true",
            "true, false, false",
            "false, false, false"
    })
    void isRestrictive(boolean notExcluded, boolean isRestrictive, boolean expectedResult) {

        AccessibilityRequest accessibilityRequest = mock(AccessibilityRequest.class);

        try (var trafficSignExclusionCalculator = Mockito.mockStatic(TrafficSignExclusionCalculator.class)) {
            try (var trafficSignRestrictionCalculator = Mockito.mockStatic(TrafficSignRestrictionCalculator.class)) {
                trafficSignExclusionCalculator.when(() -> TrafficSignExclusionCalculator.isNotExcluded(trafficSign, accessibilityRequest))
                        .thenReturn(notExcluded);
                trafficSignRestrictionCalculator.when(() -> TrafficSignRestrictionCalculator.isRestrictive(
                                trafficSign,
                                accessibilityRequest))
                        .thenReturn(isRestrictive);

                assertThat(trafficSign.isRestrictive(accessibilityRequest)).isEqualTo(expectedResult);
            }
        }
    }

    @Test
    void toStringTest() {

        assertThat(trafficSign).hasToString(
                "TrafficSign(id=1, externalId='id2', roadSectionId=3, trafficSignType=C7, fraction=0.2, networkSnappedLatitude=2.0, networkSnappedLongitude=3.0, direction=BACKWARD)");
    }

    @Test
    void validate_id_null() {

        trafficSign = trafficSign.withId(null);
        validate(
                trafficSign,
                List.of("id"),
                List.of("must not be null"));
    }

    @Test
    void validate_externalId_null() {

        trafficSign = trafficSign.withExternalId(null);
        validate(
                trafficSign,
                List.of("externalId"),
                List.of("must not be null"));
    }

    @Test
    void validate_roadSectionId_null() {

        trafficSign = trafficSign.withRoadSectionId(null);
        validate(
                trafficSign,
                List.of("roadSectionId"),
                List.of("must not be null"));
    }

    @Test
    void validate_trafficSignType_null() {

        trafficSign = trafficSign.withTrafficSignType(null);
        validate(
                trafficSign,
                List.of("trafficSignType"),
                List.of("must not be null"));
    }

    @Test
    void validate_latitude_null() {

        trafficSign = trafficSign.withLatitude(null);
        validate(
                trafficSign,
                List.of("latitude"),
                List.of("must not be null"));
    }

    @Test
    void validate_longitude_null() {

        trafficSign = trafficSign.withLongitude(null);
        validate(
                trafficSign,
                List.of("longitude"),
                List.of("must not be null"));
    }

    @Test
    void validate_direction_null() {

        trafficSign = trafficSign.withDirection(null);
        validate(
                trafficSign,
                List.of("direction"),
                List.of("must not be null"));
    }

    @Test
    void validate_fraction_null() {

        trafficSign = trafficSign.withFraction(null);
        validate(
                trafficSign,
                List.of("fraction"),
                List.of("must not be null"));
    }

    @Test
    void validate_supplementaryTrafficSigns_null() {

        trafficSign = trafficSign.withSupplementaryTrafficSigns(null);
        validate(
                trafficSign,
                List.of("supplementaryTrafficSigns"),
                List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {

        return trafficSign.getClass();
    }
}
