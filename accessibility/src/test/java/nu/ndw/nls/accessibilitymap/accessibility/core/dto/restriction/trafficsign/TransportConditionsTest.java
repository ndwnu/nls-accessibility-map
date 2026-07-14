package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransportConditionsTest {

    private static final String REQUEST_SET_CONDITION_SET_EVALUABLE_CONDITIONS_CONDITIONS_APPLY = """
            requestSetContains, conditionSetContains,   hasEvaluableConditions, conditionsApply
            NULL,               NULL,                   false,                  false
            EMPTY,              NULL,                   false,                  false
            FIRST,              NULL,                   false,                  false
            ALL,                NULL,                   false,                  false
            NULL,               EMPTY,                  false,                  false
            EMPTY,              EMPTY,                  false,                  false
            FIRST,              EMPTY,                  false,                  false
            ALL,                EMPTY,                  false,                  false
            NULL,               FIRST,                  false,                  false
            EMPTY,              FIRST,                  false,                  false
            FIRST,              FIRST,                  true,                   true
            SECOND,             FIRST,                  true,                   false
            ALL,                FIRST,                  true,                   true
            NULL,               ALL,                    false,                  false
            EMPTY,              ALL,                    false,                  false
            FIRST,              ALL,                    true,                   true
            ALL,                ALL,                    true,                   true
            """;

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

    @ParameterizedTest
    @CsvSource(textBlock = REQUEST_SET_CONDITION_SET_EVALUABLE_CONDITIONS_CONDITIONS_APPLY, useHeadersInDisplayName = true)
    void conditionsApply_and_hasEvaluableConditions_transportTypes(
            EnumSetContains requestSetContains,
            EnumSetContains conditionSetContains,
            boolean hasEvaluableConditions,
            boolean conditionsApply) {
        TransportConditions transportConditions = TransportConditions.builder()
                .transportTypes(conditionSetContains.map(TransportType.values()))
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withTransportTypes(requestSetContains.map(TransportType.values()))))
                .isEqualTo(conditionsApply);

        assertThat(transportConditions.hasEvaluableConditions(accessibilityRequest.withTransportTypes(requestSetContains.map(TransportType.values()))))
                .isEqualTo(hasEvaluableConditions);
    }

    @ParameterizedTest
    @CsvSource(textBlock = REQUEST_SET_CONDITION_SET_EVALUABLE_CONDITIONS_CONDITIONS_APPLY, useHeadersInDisplayName = true)
    void conditionsApply_and_hasEvaluableConditions_categories(
            EnumSetContains requestSetContains,
            EnumSetContains conditionSetContains,
            boolean hasEvaluableConditions,
            boolean conditionsApply) {
        TransportConditions transportConditions = TransportConditions.builder()
                .categories(conditionSetContains.map(Category.values()))
                .build();


        assertThat(transportConditions.hasEvaluableConditions(accessibilityRequest.withCategories(requestSetContains.map(Category.values()))))
                .isEqualTo(hasEvaluableConditions);

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withCategories(requestSetContains.map(Category.values()))))
                .isEqualTo(conditionsApply);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            requestSetContains, conditionSetContains,   hasEvaluableConditions, conditionsApply
            NULL,               null,                   false,                  false
            EMPTY,              null,                   false,                  false
            FIRST,              null,                   false,                  false
            ALL,                null,                   false,                  false
            NULL,               COMPRESSED_NATURAL_GAS, false,                  false
            EMPTY,              COMPRESSED_NATURAL_GAS, false,                  false
            FIRST,              COMPRESSED_NATURAL_GAS, true,                   true
            FIRST,              DIESEL,                 true,                   false
            ALL,                COMPRESSED_NATURAL_GAS, true,                   true
            """, nullValues = "null", useHeadersInDisplayName = true)
    void conditionsApply_and_hasEvaluableConditions_fuelType(
            EnumSetContains requestSetContains,
            FuelType fuelType,
            boolean hasEvaluableConditions,
            boolean conditionsApply) {
        TransportConditions transportConditions = TransportConditions.builder()
                .fuelType(fuelType)
                .build();

        assertThat(transportConditions.hasEvaluableConditions(accessibilityRequest.withFuelTypes(requestSetContains.map(FuelType.values()))))
                .isEqualTo(hasEvaluableConditions);

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withFuelTypes(requestSetContains.map(FuelType.values()))))
                .isEqualTo(conditionsApply);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            requestSetContains, conditionSetContains,   hasEvaluableConditions, conditionsApply
            NULL,               null,                   false,                  false
            EMPTY,              null,                   false,                  false
            FIRST,              null,                   false,                  false
            ALL,                null,                   false,                  false
            NULL,               EURO_1,                 false,                  false
            EMPTY,              EURO_1,                 false,                  false
            FIRST,              EURO_1,                 true,                   true
            FIRST,              EURO_2,                 true,                   false
            ALL,                EURO_1,                 true,                   true
            """, nullValues = "null", useHeadersInDisplayName = true)
    void conditionsApply_emissionClass(
            EnumSetContains requestSetContains,
            EmissionClass emissionClass,
            boolean hasEvaluableConditions,
            boolean conditionsApply) {
        TransportConditions transportConditions = TransportConditions.builder()
                .emissionClass(emissionClass)
                .build();

        assertThat(transportConditions.hasEvaluableConditions(accessibilityRequest.withEmissionClasses(requestSetContains.map(EmissionClass.values())))).isEqualTo(
                hasEvaluableConditions);

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withEmissionClasses(requestSetContains.map(EmissionClass.values())))).isEqualTo(
                conditionsApply);
    }

    @Test
    void conditionsApply_unrestricted_noRestrictions() {
        TransportConditions transportConditions = TransportConditions.unrestricted();

        assertThat(transportConditions.conditionsApply(accessibilityRequest)).isFalse();
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
    @CsvSource(textBlock = """
            vehicleLengthCondition, vehicleLengthRequest,   hasEvaluableConditions, conditionsApply
            20,                     20,                     true,                   false
            20,                     20.1,                   true,                   true
            20,                     null,                   false,                  false
            null,                   20.1,                   true,                   false
            null,                   null,                   false,                  false
            """, useHeadersInDisplayName = true, nullValues = "null")
    void conditionsApply_vehicleLength(
            Double vehicleLengthCondition,
            Double vehicleLengthRequest,
            boolean hasEvaluableConditions,
            boolean conditionsApply) {

        TransportConditions transportConditions = TransportConditions.builder()
                .vehicleLengthInCm(Maximum.builder().value(vehicleLengthCondition).build())
                .build();

        assertThat(transportConditions.hasEvaluableConditions(accessibilityRequest.withVehicleLengthInCm(vehicleLengthRequest))).isEqualTo(
                hasEvaluableConditions);

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withVehicleLengthInCm(vehicleLengthRequest))).isEqualTo(
                conditionsApply);
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

    enum EnumSetContains {
        NULL,
        EMPTY,
        FIRST,
        SECOND,
        ALL;

        public <T extends Enum<T>> Set<T> map(T[] values) {
            switch (this) {
                case NULL:
                    return null;
                case EMPTY:
                    return Collections.emptySet();
                case FIRST:
                    return Set.of(values[0]);
                case SECOND:
                    return Set.of(values[1]);
                case ALL:
            }
            return Set.of(values);
        }
    }

}