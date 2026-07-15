package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransportConditionsTest {

    private final AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder().build();

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
    void conditionsApply_unrestricted() {
        TransportConditions transportConditions = TransportConditions.unrestricted();

        assertThat(transportConditions.conditionsApply(accessibilityRequest)).isFalse();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            requestSetContains, conditionSetContains,   conditionsApply
            NULL,               NULL,                   false
            EMPTY,              NULL,                   false
            FIRST_ENUM,         NULL,                   false
            ALL,                NULL,                   false
            NULL,               EMPTY,                  false
            EMPTY,              EMPTY,                  false
            FIRST_ENUM,         EMPTY,                  false
            ALL,                EMPTY,                  false
            NULL,               FIRST_ENUM,             false
            EMPTY,              FIRST_ENUM,             false
            FIRST_ENUM,         FIRST_ENUM,             true
            SECOND_ENUM,        FIRST_ENUM,             false
            ALL,                FIRST_ENUM,             true
            NULL,               ALL,                    false
            EMPTY,              ALL,                    false
            FIRST_ENUM,         ALL,                    true
            ALL,                ALL,                    true
            """, useHeadersInDisplayName = true)
    void conditionsApply_transportTypes(
            EnumSetContains requestSetContains,
            EnumSetContains conditionSetContains,
            boolean conditionsApply) {
        TransportConditions transportConditions = TransportConditions.builder()
                .transportTypes(conditionSetContains.map(TransportType.values()))
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withTransportTypes(requestSetContains.map(TransportType.values()))))
                .isEqualTo(conditionsApply);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            requestSetContains, conditionSetContains,   conditionsApply
            NULL,               NULL,                   false
            EMPTY,              NULL,                   false
            FIRST_ENUM,         NULL,                   false
            ALL,                NULL,                   false
            NULL,               EMPTY,                  false
            EMPTY,              EMPTY,                  false
            FIRST_ENUM,         EMPTY,                  false
            ALL,                EMPTY,                  false
            NULL,               FIRST_ENUM,             false
            EMPTY,              FIRST_ENUM,             false
            FIRST_ENUM,         FIRST_ENUM,             true
            SECOND_ENUM,        FIRST_ENUM,             false
            ALL,                FIRST_ENUM,             true
            NULL,               ALL,                    false
            EMPTY,              ALL,                    false
            FIRST_ENUM,         ALL,                    true
            ALL,                ALL,                    true
            """, useHeadersInDisplayName = true)
    void conditionsApply_categories(
            EnumSetContains requestSetContains,
            EnumSetContains conditionSetContains,
            boolean conditionsApply) {
        TransportConditions transportConditions = TransportConditions.builder()
                .categories(conditionSetContains.map(Category.values()))
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withCategories(requestSetContains.map(Category.values()))))
                .isEqualTo(conditionsApply);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            requestSetContains, emissionClass,  conditionsApply
            NULL,               null,           false
            EMPTY,              null,           false
            FIRST_ENUM,         null,           false
            ALL,                null,           false
            NULL,               EURO_1,         false
            EMPTY,              EURO_1,         false
            FIRST_ENUM,         EURO_1,         true
            FIRST_ENUM,         EURO_2,         false
            ALL,                EURO_1,         true
            """, nullValues = "null", useHeadersInDisplayName = true)
    void conditionsApply_emissionClass(
            EnumSetContains requestSetContains,
            EmissionClass emissionClass,
            boolean conditionsApply) {
        TransportConditions transportConditions = TransportConditions.builder()
                .emissionClass(emissionClass)
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withEmissionClasses(requestSetContains.map(EmissionClass.values()))))
                .isEqualTo(conditionsApply);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            requestSetContains, conditionSetContains,   conditionsApply
            NULL,               null,                   false
            EMPTY,              null,                   false
            FIRST_ENUM,         null,                   false
            ALL,                null,                   false
            NULL,               COMPRESSED_NATURAL_GAS, false
            EMPTY,              COMPRESSED_NATURAL_GAS, false
            FIRST_ENUM,         COMPRESSED_NATURAL_GAS, true
            FIRST_ENUM,         DIESEL,                 false
            ALL,                COMPRESSED_NATURAL_GAS, true
            """, nullValues = "null", useHeadersInDisplayName = true)
    void conditionsApply_fuelType(EnumSetContains requestSetContains, FuelType fuelType, boolean conditionsApply) {
        TransportConditions transportConditions = TransportConditions.builder().fuelType(fuelType).build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withFuelTypes(requestSetContains.map(FuelType.values()))))
                .isEqualTo(conditionsApply);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            vehicleLengthCondition, vehicleLengthRequest,   conditionsApply
            20,                     20,                     false
            20,                     20.1,                   true
            20,                     null,                   false
            null,                   20.1,                   false
            null,                   null,                   false
            """, useHeadersInDisplayName = true, nullValues = "null")
    void conditionsApply_vehicleLengthInCm(
            Double vehicleLengthCondition,
            Double vehicleLengthRequest,
            boolean conditionsApply) {

        TransportConditions transportConditions = TransportConditions.builder()
                .vehicleLengthInCm(Maximum.builder().value(vehicleLengthCondition).build())
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withVehicleLengthInCm(vehicleLengthRequest))).isEqualTo(
                conditionsApply);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            vehicleHeightCondition, vehicleHeightRequest,   conditionsApply
            20,                     20,                     false
            20,                     20.1,                   true
            20,                     null,                   false
            null,                   20.1,                   false
            null,                   null,                   false
            """, useHeadersInDisplayName = true, nullValues = "null")
    void conditionsApply_vehicleHeightInCm(
            Double vehicleHeightCondition,
            Double vehicleHeightRequest,
            boolean conditionsApply) {

        TransportConditions transportConditions = TransportConditions.builder()
                .vehicleHeightInCm(Maximum.builder()
                        .value(vehicleHeightCondition)
                        .build())
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withVehicleHeightInCm(vehicleHeightRequest))).isEqualTo(
                conditionsApply);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            vehicleWidthCondition,  vehicleWidthRequest,    conditionsApply
            20,                     20,                     false
            20,                     20.1,                   true
            20,                     null,                   false
            null,                   20.1,                   false
            null,                   null,                   false
            """, useHeadersInDisplayName = true, nullValues = "null")
    void conditionsApply_vehicleWidthInCm(
            Double vehicleWidthCondition,
            Double vehicleWidthRequest,
            boolean conditionsApply) {

        TransportConditions transportConditions = TransportConditions.builder()
                .vehicleWidthInCm(Maximum.builder()
                        .value(vehicleWidthCondition)
                        .build())
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withVehicleWidthInCm(vehicleWidthRequest))).isEqualTo(conditionsApply);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            vehicleWeightCondition, vehicleWeightRequest,   conditionsApply
            20,                     20,                     false
            20,                     20.1,                   true
            20,                     null,                   false
            null,                   20.1,                   false
            null,                   null,                   false
            """, useHeadersInDisplayName = true, nullValues = "null")
    void conditionsApply_vehicleWeightInKg(
            Double vehicleWeightCondition,
            Double vehicleWeightRequest,
            boolean conditionsApply) {

        TransportConditions transportConditions = TransportConditions.builder()
                .vehicleWeightInKg(Maximum.builder()
                        .value(vehicleWeightCondition)
                        .build())
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withVehicleWeightInKg(vehicleWeightRequest))).isEqualTo(
                conditionsApply);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            vehicleAxleLoadCondition,   vehicleAxleLoadRequest, conditionsApply
            20,                         20,                     false
            20,                         20.1,                   true
            20,                         null,                   false
            null,                       20.1,                   false
            null,                       null,                   false
            """, useHeadersInDisplayName = true, nullValues = "null")
    void conditionsApply_vehicleAxleLoadInKg(
            Double vehicleAxleLoadCondition,
            Double vehicleAxleLoadRequest,
            boolean conditionsApply) {

        TransportConditions transportConditions = TransportConditions.builder()
                .vehicleAxleLoadInKg(Maximum.builder()
                        .value(vehicleAxleLoadCondition)
                        .build())
                .build();

        assertThat(transportConditions.conditionsApply(accessibilityRequest.withVehicleAxleLoadInKg(vehicleAxleLoadRequest))).isEqualTo(
                conditionsApply);
    }

    enum EnumSetContains {
        NULL,
        EMPTY,
        FIRST_ENUM,
        SECOND_ENUM,
        ALL;

        public <T extends Enum<T>> Set<T> map(T[] values) {
            switch (this) {
                case NULL:
                    return null;
                case EMPTY:
                    return Collections.emptySet();
                case FIRST_ENUM:
                    return Set.of(values[0]);
                case SECOND_ENUM:
                    return Set.of(values[1]);
                case ALL:
            }
            return Set.of(values);
        }
    }

}