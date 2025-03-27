package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @Test
    void hasActiveRestrictions() {

        restrictions = Restrictions.builder()
                .transportTypes(TransportType.allExcept())
                .build();

        assertThat(restrictions.hasActiveRestrictions(accessibilityRequest.withTransportTypes(TransportType.allExcept()))).isTrue();
    }

    @Test
    void hasActiveRestrictions_noRestrictions() {

        restrictions = Restrictions.builder().build();

        assertThat(restrictions.hasActiveRestrictions(accessibilityRequest)).isFalse();
    }

    @Test
    void isRestrictive_combinationTest() {

        restrictions = Restrictions.builder()
                .transportTypes(Set.of(TransportType.TRUCK))
                .vehicleLengthInCm(Maximum.builder().value(10d).build())
                .vehicleWidthInCm(Maximum.builder().value(20d).build())
                .vehicleHeightInCm(Maximum.builder().value(30d).build())
                .vehicleWeightInKg(Maximum.builder().value(40d).build())
                .vehicleAxleLoadInKg(Maximum.builder().value(50d).build())
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest
                .withTransportTypes(Set.of(TransportType.TRUCK))
                .withVehicleLengthInCm(restrictions.vehicleLengthInCm().value() + 1d)
                .withVehicleWidthInCm(restrictions.vehicleWidthInCm().value() + 1d)
                .withVehicleHeightInCm(restrictions.vehicleHeightInCm().value() + 1d)
                .withVehicleWeightInKg(restrictions.vehicleWeightInKg().value() + 1d)
                .withVehicleAxleLoadInKg(restrictions.vehicleAxleLoadInKg().value() + 1d)
        )).isTrue();

        assertThat(restrictions.isRestrictive(accessibilityRequest
                .withTransportTypes(Set.of(TransportType.BUS))
                .withVehicleLengthInCm(restrictions.vehicleLengthInCm().value() - 1d)
                .withVehicleWidthInCm(restrictions.vehicleWidthInCm().value() - 1d)
                .withVehicleHeightInCm(restrictions.vehicleHeightInCm().value() - 1d)
                .withVehicleWeightInKg(restrictions.vehicleWeightInKg().value() - 1d)
                .withVehicleAxleLoadInKg(restrictions.vehicleAxleLoadInKg().value() - 1d)
        )).isFalse();
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void isRestrictive_transportType_isRestrictive(TransportType transportType) {

        restrictions = Restrictions.builder()
                .transportTypes(Set.of(transportType))
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withTransportTypes(Set.of(transportType)))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void isRestrictive_transportType_notRestrictive(TransportType transportType) {

        restrictions = Restrictions.builder()
                .transportTypes(TransportType.allExcept(transportType))
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withTransportTypes(Set.of(transportType)))).isFalse();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true, true,
            true, false, false,
            false, true, false,
            false, false, false,
            """)
    void isRestrictive_transportType_null(
            boolean restrictionHasTransportTypes,
            boolean accessibilityRequestHasTransportTypes,
            boolean expectedResult) {

        restrictions = Restrictions.builder()
                .transportTypes(restrictionHasTransportTypes ? TransportType.allExcept() : null)
                .build();

        assertThat(restrictions.isRestrictive(
                accessibilityRequest.withTransportTypes(accessibilityRequestHasTransportTypes ? TransportType.allExcept() : null))
        ).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            20, 20, false
            20, 20.1, true
            20, , false
            , 20.1, false
            , , false
            """)
    void isRestrictive_vehicleLength(
            Double vehicleLength,
            Double vehicleLengthToTestFor,
            boolean expectedResult) {

        restrictions = Restrictions.builder()
                .vehicleLengthInCm(Maximum.builder().value(vehicleLength).build())
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withVehicleLengthInCm(vehicleLengthToTestFor))).isEqualTo(
                expectedResult);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            20, 20, false
            20, 20.1, true
            20, , false
            , 20.1, false
            , , false
            """)
    void isRestrictive_vehicleHeight(
            Double vehicleHeight,
            Double vehicleHeightToTestFor,
            boolean expectedResult) {

        restrictions = Restrictions.builder()
                .vehicleHeightInCm(Maximum.builder()
                        .value(vehicleHeight)
                        .build())
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withVehicleHeightInCm(vehicleHeightToTestFor))).isEqualTo(
                expectedResult);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            20, 20, false
            20, 20.1, true
            20, , false
            , 20.1, false
            , , false
            """)
    void isRestrictive_vehicleWidth(
            Double vehicleWidth,
            Double vehicleWidthToTestFor,
            boolean expectedResult) {

        restrictions = Restrictions.builder()
                .vehicleWidthInCm(Maximum.builder()
                        .value(vehicleWidth)
                        .build())
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withVehicleWidthInCm(vehicleWidthToTestFor))).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            20, 20, false
            20, 20.1, true
            20, , false
            , 20.1, false
            , , false
            """)
    void isRestrictive_vehicleWeight(
            Double vehicleWeight,
            Double vehicleWeightToTestFor,
            boolean expectedResult) {

        restrictions = Restrictions.builder()
                .vehicleWeightInKg(Maximum.builder()
                        .value(vehicleWeight)
                        .build())
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withVehicleWeightInKg(vehicleWeightToTestFor))).isEqualTo(
                expectedResult);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            20, 20, false
            20, 20.1, true
            20, , false
            , 20.1, false
            , , false
            """)
    void isRestrictive_vehicleAxleLoad(
            Double vehicleAxleLoad,
            Double vehicleAxleLoadToTestFor,
            boolean expectedResult) {

        restrictions = Restrictions.builder()
                .vehicleAxleLoadInKg(Maximum.builder()
                        .value(vehicleAxleLoad)
                        .build())
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withVehicleAxleLoadInKg(vehicleAxleLoadToTestFor))).isEqualTo(
                expectedResult);
    }
}