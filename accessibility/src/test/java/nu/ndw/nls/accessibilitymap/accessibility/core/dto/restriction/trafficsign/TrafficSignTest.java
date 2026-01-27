package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.Mockito;

class TrafficSignTest extends ValidationTest {

    private TrafficSign trafficSign;

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
                .textSigns(List.of())
                .trafficSignType(TrafficSignType.C7)
                .transportRestrictions(TransportRestrictions.builder().build())
                .build();
    }

    @Test
    void hasTimeWindowedSign() {

        trafficSign = trafficSign.withTextSigns(List.of(
                TextSign.builder()
                        .type(TextSignType.EXCLUDING)
                        .build(),
                TextSign.builder()
                        .type(TextSignType.TIME_PERIOD)
                        .build()
        ));

        assertThat(trafficSign.hasTimeWindowedSign()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = TextSignType.class, mode = Mode.EXCLUDE, names = "TIME_PERIOD")
    void hasTimeWindowedSign_hasNoTimedWindowSign(TextSignType textSignType) {

        trafficSign = trafficSign.withTextSigns(List.of(
                TextSign.builder()
                        .type(textSignType)
                        .build()
        ));

        assertThat(trafficSign.hasTimeWindowedSign()).isFalse();
    }

    @Test
    void findFirstTimeWindowedSign() {

        trafficSign = trafficSign.withTextSigns(List.of(
                TextSign.builder()
                        .type(TextSignType.TIME_PERIOD)
                        .text("1")
                        .build(),
                TextSign.builder()
                        .type(TextSignType.TIME_PERIOD)
                        .text("2")
                        .build()
        ));

        assertThat(trafficSign.findFirstTimeWindowedSign().get().text()).isEqualTo("1");
    }

    @Test
    void findFirstTimeWindowedSign_nothingFound() {

        trafficSign = trafficSign.withTextSigns(List.of(
                TextSign.builder()
                        .type(TextSignType.EXCLUDING)
                        .text("1")
                        .build()
        ));

        assertThat(trafficSign.findFirstTimeWindowedSign()).isEmpty();
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
                "TrafficSign{id=1, externalId='id2', roadSectionId=3, trafficSignType=C7, fraction=0.2, networkSnappedLatitude=2.0, networkSnappedLongitude=3.0, direction=BACKWARD}");
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
    void validate_textSigns_null() {

        trafficSign = trafficSign.withTextSigns(null);
        validate(
                trafficSign,
                List.of("textSigns"),
                List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {

        return trafficSign.getClass();
    }
}
