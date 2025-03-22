package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.dto.value.Range;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.dto.value.TransportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestrictionsTest {

    private Restrictions restrictions;

    private AccessibilityRequest accessibilityRequest;

    @BeforeEach
    void setUp() {

        accessibilityRequest = AccessibilityRequest.builder().build();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true,
            false, false
            , false
            """)
    void isRestrictive_isBlocked(Boolean isRestrictive, boolean expectedResult) {

        restrictions = Restrictions.builder()
                .isBlocked(isRestrictive)
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void isRestrictive_transportType_isRestrictive(TransportType transportType) {

        restrictions = Restrictions.builder()
                .transportTypes(List.of(transportType))
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withTransportType(transportType))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void isRestrictive_transportType_notRestrictive(TransportType transportType) {

        restrictions = Restrictions.builder()
                .transportTypes(
                        Arrays.stream(TransportType.values())
                                .filter(t -> !t.equals(transportType))
                                .toList())
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withTransportType(transportType))).isFalse();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            10, 20, 19.9, false
            10, 20, 10.1, false
            10, 20, 9.9, true
            10, 20, 20.1, true
            10, , 10.1, false
            10, , 9.9, true
            , 10, 10.1, true
            , 10, 9.9, false
            , , 9.9, false
            """)
    void isRestrictive_vehicleLength(
            Double vehicleLengthMin,
            Double vehicleLengthMax,
            Double vehicleLengthToTestFor,
            boolean expectedResult) {

        restrictions = Restrictions.builder()
                .vehicleLength(Range.builder()
                        .min(vehicleLengthMin)
                        .max(vehicleLengthMax)
                        .build())
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withVehicleLength(vehicleLengthToTestFor))).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            10, 20, 19.9, false
            10, 20, 10.1, false
            10, 20, 9.9, true
            10, 20, 20.1, true
            10, , 10.1, false
            10, , 9.9, true
            , 10, 10.1, true
            , 10, 9.9, false
            , , 9.9, false
            """)
    void isRestrictive_vehicleHeight(
            Double vehicleHeightMin,
            Double vehicleHeightMax,
            Double vehicleHeightToTestFor,
            boolean expectedResult) {

        restrictions = Restrictions.builder()
                .vehicleHeight(Range.builder()
                        .min(vehicleHeightMin)
                        .max(vehicleHeightMax)
                        .build())
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withVehicleHeight(vehicleHeightToTestFor))).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            10, 20, 19.9, false
            10, 20, 10.1, false
            10, 20, 9.9, true
            10, 20, 20.1, true
            10, , 10.1, false
            10, , 9.9, true
            , 10, 10.1, true
            , 10, 9.9, false
            , , 9.9, false
            """)
    void isRestrictive_vehicleWeight(
            Double vehicleWeightMin,
            Double vehicleWeightMax,
            Double vehicleWeightToTestFor,
            boolean expectedResult) {

        restrictions = Restrictions.builder()
                .vehicleWeight(Range.builder()
                        .min(vehicleWeightMin)
                        .max(vehicleWeightMax)
                        .build())
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withVehicleWeight(vehicleWeightToTestFor))).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            10, 20, 19.9, false
            10, 20, 10.1, false
            10, 20, 9.9, true
            10, 20, 20.1, true
            10, , 10.1, false
            10, , 9.9, true
            , 10, 10.1, true
            , 10, 9.9, false
            , , 9.9, false
            """)
    void isRestrictive_vehicleWidth(
            Double vehicleWidthMin,
            Double vehicleWidthMax,
            Double vehicleWidthToTestFor,
            boolean expectedResult) {

        restrictions = Restrictions.builder()
                .vehicleWidth(Range.builder()
                        .min(vehicleWidthMin)
                        .max(vehicleWidthMax)
                        .build())
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withVehicleWidth(vehicleWidthToTestFor))).isEqualTo(expectedResult);
    }


    @ParameterizedTest
    @CsvSource(textBlock = """
            10, 20, 19.9, false
            10, 20, 10.1, false
            10, 20, 9.9, true
            10, 20, 20.1, true
            10, , 10.1, false
            10, , 9.9, true
            , 10, 10.1, true
            , 10, 9.9, false
            , , 9.9, false
            """)
    void isRestrictive_vehicleAxleLoad(
            Double vehicleAxleLoadMin,
            Double vehicleAxleLoadMax,
            Double vehicleAxleLoadToTestFor,
            boolean expectedResult) {

        restrictions = Restrictions.builder()
                .vehicleAxleLoad(Range.builder()
                        .min(vehicleAxleLoadMin)
                        .max(vehicleAxleLoadMax)
                        .build())
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withVehicleAxleLoad(vehicleAxleLoadToTestFor))).isEqualTo(expectedResult);
    }
}