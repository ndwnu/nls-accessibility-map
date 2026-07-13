package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransportConditionsTest {

    private AccessibilityRequest accessibilityRequest;

    @BeforeEach
    void setUp() {
        accessibilityRequest = AccessibilityRequest.builder().build();
    }

    @Test
    void unrestricted() {
        TransportConditions unrestricted = TransportConditions.unrestricted();

        assertThat(unrestricted).isNotNull()
                .isSameAs(TransportConditions.unrestricted());

        assertThat(unrestricted.transportTypes()).isNull();
        assertThat(unrestricted.categories()).isNull();
        assertThat(unrestricted.timeValidity()).isNull();
        assertThat(unrestricted.emissionClass()).isNull();
        assertThat(unrestricted.fuelType()).isNull();
        assertThat(unrestricted.vehicleLengthInCm()).isNull();
        assertThat(unrestricted.vehicleHeightInCm()).isNull();
        assertThat(unrestricted.vehicleWidthInCm()).isNull();
        assertThat(unrestricted.vehicleWeightInKg()).isNull();
        assertThat(unrestricted.vehicleAxleLoadInKg()).isNull();
    }

    @Test
    void hasEvaluableConditions() {

        TransportConditions transportConditions = TransportConditions.builder()
                .transportTypes(TransportType.allExcept())
                .build();

        assertThat(transportConditions.hasEvaluableConditions(accessibilityRequest.withTransportTypes(TransportType.allExcept()))).isTrue();
    }

    @Test
    void hasEvaluableConditions_unrestricted_noRestrictions() {

        TransportConditions transportConditions = TransportConditions.unrestricted();

        assertThat(transportConditions.hasEvaluableConditions(accessibilityRequest)).isFalse();
    }

    @Test
    void hasEvaluableConditions_noRestrictions() {

        TransportConditions transportConditions = TransportConditions.builder().build();

        assertThat(transportConditions.hasEvaluableConditions(accessibilityRequest)).isFalse();
    }

    @Test
    void conditionsApply_combinationTest() {

        TransportConditions transportConditions = TransportConditions.builder()
                .transportTypes(Set.of(TransportType.TRUCK))
                .vehicleLengthInCm(Maximum.builder().value(10d).build())
                .vehicleWidthInCm(Maximum.builder().value(20d).build())
                .vehicleHeightInCm(Maximum.builder().value(30d).build())
                .vehicleWeightInKg(Maximum.builder().value(40d).build())
                .vehicleAxleLoadInKg(Maximum.builder().value(50d).build())
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest
                .withTransportTypes(Set.of(TransportType.TRUCK))
                .withVehicleLengthInCm(transportConditions.vehicleLengthInCm().value() + 1d)
                .withVehicleWidthInCm(transportConditions.vehicleWidthInCm().value() + 1d)
                .withVehicleHeightInCm(transportConditions.vehicleHeightInCm().value() + 1d)
                .withVehicleWeightInKg(transportConditions.vehicleWeightInKg().value() + 1d)
                .withVehicleAxleLoadInKg(transportConditions.vehicleAxleLoadInKg().value() + 1d)
        )).isTrue();

        assertThat(transportConditions.conditionsApply(accessibilityRequest
                .withTransportTypes(Set.of(TransportType.BUS))
                .withVehicleLengthInCm(transportConditions.vehicleLengthInCm().value() - 1d)
                .withVehicleWidthInCm(transportConditions.vehicleWidthInCm().value() - 1d)
                .withVehicleHeightInCm(transportConditions.vehicleHeightInCm().value() - 1d)
                .withVehicleWeightInKg(transportConditions.vehicleWeightInKg().value() - 1d)
                .withVehicleAxleLoadInKg(transportConditions.vehicleAxleLoadInKg().value() - 1d)
        )).isFalse();
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void conditionsApply_transportType_conditionsApply(TransportType transportType) {

        TransportConditions transportConditions = TransportConditions.builder()
                .transportTypes(Set.of(transportType))
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withTransportTypes(Set.of(transportType)))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void conditionsApply_transportType_notRestrictive(TransportType transportType) {

        TransportConditions transportConditions = TransportConditions.builder()
                .transportTypes(TransportType.allExcept(transportType))
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withTransportTypes(Set.of(transportType)))).isFalse();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true, true,
            true, false, false,
            false, true, false,
            false, false, false,
            """)
    void conditionsApply_transportType_null(
            boolean restrictionHasTransportTypes,
            boolean accessibilityRequestHasTransportTypes,
            boolean expectedResult) {

        TransportConditions transportConditions = TransportConditions.builder()
                .transportTypes(restrictionHasTransportTypes ? TransportType.allExcept() : null)
                .build();

        assertThat(transportConditions.conditionsApply(
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
    void conditionsApply_vehicleLength(
            Double vehicleLength,
            Double vehicleLengthToTestFor,
            boolean expectedResult) {

        TransportConditions transportConditions = TransportConditions.builder()
                .vehicleLengthInCm(Maximum.builder().value(vehicleLength).build())
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withVehicleLengthInCm(vehicleLengthToTestFor))).isEqualTo(
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
    void conditionsApply_vehicleHeight(
            Double vehicleHeight,
            Double vehicleHeightToTestFor,
            boolean expectedResult) {

        TransportConditions transportConditions = TransportConditions.builder()
                .vehicleHeightInCm(Maximum.builder()
                        .value(vehicleHeight)
                        .build())
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withVehicleHeightInCm(vehicleHeightToTestFor))).isEqualTo(
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
    void conditionsApply_vehicleWidth(
            Double vehicleWidth,
            Double vehicleWidthToTestFor,
            boolean expectedResult) {

        TransportConditions transportConditions = TransportConditions.builder()
                .vehicleWidthInCm(Maximum.builder()
                        .value(vehicleWidth)
                        .build())
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withVehicleWidthInCm(vehicleWidthToTestFor))).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            20, 20, false
            20, 20.1, true
            20, , false
            , 20.1, false
            , , false
            """)
    void conditionsApply_vehicleWeight(
            Double vehicleWeight,
            Double vehicleWeightToTestFor,
            boolean expectedResult) {

        TransportConditions transportConditions = TransportConditions.builder()
                .vehicleWeightInKg(Maximum.builder()
                        .value(vehicleWeight)
                        .build())
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withVehicleWeightInKg(vehicleWeightToTestFor))).isEqualTo(
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
    void conditionsApply_vehicleAxleLoad(
            Double vehicleAxleLoad,
            Double vehicleAxleLoadToTestFor,
            boolean expectedResult) {

        TransportConditions transportConditions = TransportConditions.builder()
                .vehicleAxleLoadInKg(Maximum.builder()
                        .value(vehicleAxleLoad)
                        .build())
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withVehicleAxleLoadInKg(vehicleAxleLoadToTestFor))).isEqualTo(
                expectedResult);
    }

}