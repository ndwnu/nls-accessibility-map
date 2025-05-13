package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestrictionsTest {

    private AccessibilityRequest accessibilityRequest;

    private OffsetDateTime timestamp;

    @Mock
    private EmissionZone emissionZone;

    @BeforeEach
    void setUp() {

        timestamp = OffsetDateTime.parse("2022-03-11T09:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        accessibilityRequest = AccessibilityRequest.builder().build();
    }

    @Test
    void hasActiveRestrictions() {

        Restrictions restrictions = Restrictions.builder()
                .transportTypes(TransportType.allExcept())
                .build();

        assertThat(restrictions.hasActiveRestrictions(accessibilityRequest.withTransportTypes(TransportType.allExcept()))).isTrue();
    }

    @Test
    void hasActiveRestrictions_noRestrictions() {

        Restrictions restrictions = Restrictions.builder().build();

        assertThat(restrictions.hasActiveRestrictions(accessibilityRequest)).isFalse();
    }

    @Test
    void isRestrictive_combinationTest() {

        Restrictions restrictions = Restrictions.builder()
                .transportTypes(Set.of(TransportType.TRUCK))
                .vehicleLengthInCm(Maximum.builder().value(10d).build())
                .vehicleWidthInCm(Maximum.builder().value(20d).build())
                .vehicleHeightInCm(Maximum.builder().value(30d).build())
                .vehicleWeightInKg(Maximum.builder().value(40d).build())
                .vehicleAxleLoadInKg(Maximum.builder().value(50d).build())
                .emissionZone(emissionZone)
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

        Restrictions restrictions = Restrictions.builder()
                .transportTypes(Set.of(transportType))
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withTransportTypes(Set.of(transportType)))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void isRestrictive_transportType_notRestrictive(TransportType transportType) {

        Restrictions restrictions = Restrictions.builder()
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

        Restrictions restrictions = Restrictions.builder()
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

        Restrictions restrictions = Restrictions.builder()
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

        Restrictions restrictions = Restrictions.builder()
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

        Restrictions restrictions = Restrictions.builder()
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

        Restrictions restrictions = Restrictions.builder()
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

        Restrictions restrictions = Restrictions.builder()
                .vehicleAxleLoadInKg(Maximum.builder()
                        .value(vehicleAxleLoad)
                        .build())
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest.withVehicleAxleLoadInKg(vehicleAxleLoadToTestFor))).isEqualTo(
                expectedResult);
    }

    @Test
    void isRestrictive_emissionZone_restrictive() {

        accessibilityRequest = accessibilityRequest
                .withVehicleWeightInKg(2d)
                .withTransportTypes(Set.of(TransportType.CAR))
                .withTimestamp(timestamp)
                .withFuelTypes(Set.of(FuelType.DIESEL))
                .withEmissionClasses(Set.of(EmissionClass.EURO_4));

        when(emissionZone.isActive(timestamp)).thenReturn(true);
        when(emissionZone.isRelevant(
                accessibilityRequest.vehicleWeightInKg(),
                accessibilityRequest.fuelTypes(),
                accessibilityRequest.transportTypes())
        ).thenReturn(true);
        when(emissionZone.isExempt(
                timestamp,
                accessibilityRequest.vehicleWeightInKg(),
                accessibilityRequest.emissionClasses(),
                accessibilityRequest.transportTypes())
        ).thenReturn(false);

        Restrictions restrictions = Restrictions.builder()
                .emissionZone(emissionZone)
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest)).isTrue();
    }

    @Test
    void isRestrictive_emissionZone_isExempt() {

        accessibilityRequest = accessibilityRequest
                .withVehicleWeightInKg(2d)
                .withTransportTypes(Set.of(TransportType.CAR))
                .withTimestamp(timestamp)
                .withFuelTypes(Set.of(FuelType.DIESEL))
                .withEmissionClasses(Set.of(EmissionClass.EURO_4));

        when(emissionZone.isActive(timestamp)).thenReturn(true);
        when(emissionZone.isRelevant(
                accessibilityRequest.vehicleWeightInKg(),
                accessibilityRequest.fuelTypes(),
                accessibilityRequest.transportTypes())
        ).thenReturn(true);
        when(emissionZone.isExempt(
                timestamp,
                accessibilityRequest.vehicleWeightInKg(),
                accessibilityRequest.emissionClasses(),
                accessibilityRequest.transportTypes())
        ).thenReturn(true);

        Restrictions restrictions = Restrictions.builder()
                .emissionZone(emissionZone)
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest)).isFalse();
    }

    @Test
    void isRestrictive_emissionZone_notRelevant() {

        accessibilityRequest = accessibilityRequest
                .withVehicleWeightInKg(2d)
                .withTransportTypes(Set.of(TransportType.CAR))
                .withTimestamp(timestamp)
                .withFuelTypes(Set.of(FuelType.DIESEL))
                .withEmissionClasses(Set.of(EmissionClass.EURO_4));

        when(emissionZone.isActive(timestamp)).thenReturn(true);
        when(emissionZone.isRelevant(
                accessibilityRequest.vehicleWeightInKg(),
                accessibilityRequest.fuelTypes(),
                accessibilityRequest.transportTypes())
        ).thenReturn(false);

        Restrictions restrictions = Restrictions.builder()
                .emissionZone(emissionZone)
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest)).isFalse();
    }

    @Test
    void isRestrictive_emissionZone_notActive() {

        accessibilityRequest = accessibilityRequest
                .withVehicleWeightInKg(2d)
                .withTransportTypes(Set.of(TransportType.CAR))
                .withTimestamp(timestamp)
                .withFuelTypes(Set.of(FuelType.DIESEL))
                .withEmissionClasses(Set.of(EmissionClass.EURO_4));

        when(emissionZone.isActive(timestamp)).thenReturn(false);

        Restrictions restrictions = Restrictions.builder()
                .emissionZone(emissionZone)
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest)).isFalse();
    }

    @Test
    void isRestrictive_emissionZone_requestHasNoFuelTypes_shouldIgnoreEmissionZones() {

        accessibilityRequest = accessibilityRequest
                .withVehicleWeightInKg(2d)
                .withTransportTypes(Set.of(TransportType.CAR))
                .withTimestamp(timestamp)
                .withFuelTypes(null)
                .withEmissionClasses(Set.of(EmissionClass.EURO_4));

        when(emissionZone.isActive(timestamp)).thenReturn(true);

        Restrictions restrictions = Restrictions.builder()
                .emissionZone(emissionZone)
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest)).isFalse();
    }

    @Test
    void isRestrictive_emissionZone_requestHasNoEmissionClasses_shouldIgnoreEmissionZones() {

        accessibilityRequest = accessibilityRequest
                .withVehicleWeightInKg(2d)
                .withTransportTypes(Set.of(TransportType.CAR))
                .withTimestamp(timestamp)
                .withFuelTypes(Set.of(FuelType.DIESEL))
                .withEmissionClasses(null);

        when(emissionZone.isActive(timestamp)).thenReturn(true);

        Restrictions restrictions = Restrictions.builder()
                .emissionZone(emissionZone)
                .build();

        assertThat(restrictions.isRestrictive(accessibilityRequest)).isFalse();
    }
}