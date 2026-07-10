package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransportRestrictionsTest {

    @Mock
    private EmissionZone emissionZone;

    @Mock
    private TransportConditions restrictions;

    @Mock
    private TransportConditions exceptionsA;

    @Mock
    private TransportConditions exceptionsB;

    private TransportRestrictions transportRestrictions;

    private OffsetDateTime timestamp;

    @Mock
    private AccessibilityRequest accessibilityRequest;

    @BeforeEach
    void setUp() {
        timestamp = OffsetDateTime.parse("2022-03-11T09:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        transportRestrictions = TransportRestrictions.builder()
                .emissionZone(emissionZone)
                .restrictions(restrictions)
                .exemptions(List.of(exceptionsA, exceptionsB))
                .build();
    }

    @Test
    void isRestrictive_restrictionConditionsDoNotApply() {
        when(restrictions.conditionsApply(accessibilityRequest)).thenReturn(false);
        assertThat(transportRestrictions.isRestrictive(accessibilityRequest)).isFalse();
        verifyNoInteractions(exceptionsA, exceptionsB);
    }

    @Test
    void isRestrictive_restrictionConditionApplyNoExceptions() {
        when(restrictions.conditionsApply(accessibilityRequest)).thenReturn(true);
        when(exceptionsA.conditionsApply(accessibilityRequest)).thenReturn(false);
        when(exceptionsA.conditionsApply(accessibilityRequest)).thenReturn(false);

        assertThat(transportRestrictions.isRestrictive(accessibilityRequest)).isTrue();
    }

    @Test
    void isRestrictive_restrictionConditionAndExemptionAApplies() {
        when(restrictions.conditionsApply(accessibilityRequest)).thenReturn(true);
        when(exceptionsA.conditionsApply(accessibilityRequest)).thenReturn(true);

        assertThat(transportRestrictions.isRestrictive(accessibilityRequest)).isFalse();
    }

    @Test
    void isRestrictive_restrictionConditionAndExemptionBApplies() {
        when(restrictions.conditionsApply(accessibilityRequest)).thenReturn(true);
        when(exceptionsA.conditionsApply(accessibilityRequest)).thenReturn(false);
        when(exceptionsB.conditionsApply(accessibilityRequest)).thenReturn(true);

        assertThat(transportRestrictions.isRestrictive(accessibilityRequest)).isFalse();
    }

    @Test
    void conditionsApply_emissionZone_restrictive() {
        accessibilityRequest = AccessibilityRequest.builder().build();
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

        TransportRestrictions transportConditions = TransportRestrictions.builder()
                .emissionZone(emissionZone)
                .restrictions(TransportConditions.builder().build())
                .exemptions(Collections.emptyList())
                .build();

        assertThat(transportConditions.isRestrictive(accessibilityRequest)).isTrue();
    }

    @Test
    void conditionsApply_emissionZone_isExempt() {
        accessibilityRequest = AccessibilityRequest.builder().build();
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

        TransportRestrictions transportConditions = TransportRestrictions.builder()
                .emissionZone(emissionZone)
                .restrictions(TransportConditions.builder().build())
                .exemptions(Collections.emptyList())
                .build();

        assertThat(transportConditions.isRestrictive(accessibilityRequest)).isFalse();
    }

    @Test
    void conditionsApply_emissionZone_notRelevant() {
        accessibilityRequest = AccessibilityRequest.builder().build();
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

        TransportRestrictions transportConditions = TransportRestrictions.builder()
                .emissionZone(emissionZone)
                .restrictions(TransportConditions.builder().build())
                .exemptions(Collections.emptyList())
                .build();

        assertThat(transportConditions.isRestrictive(accessibilityRequest)).isFalse();
    }

    @Test
    void conditionsApply_emissionZone_notActive() {
        accessibilityRequest = AccessibilityRequest.builder().build();
        accessibilityRequest = accessibilityRequest
                .withVehicleWeightInKg(2d)
                .withTransportTypes(Set.of(TransportType.CAR))
                .withTimestamp(timestamp)
                .withFuelTypes(Set.of(FuelType.DIESEL))
                .withEmissionClasses(Set.of(EmissionClass.EURO_4));

        when(emissionZone.isActive(timestamp)).thenReturn(false);

        TransportRestrictions transportConditions = TransportRestrictions.builder()
                .emissionZone(emissionZone)
                .restrictions(TransportConditions.builder().build())
                .exemptions(Collections.emptyList())
                .build();

        assertThat(transportConditions.isRestrictive(accessibilityRequest)).isFalse();
    }

    @Test
    void conditionsApply_emissionZone_requestHasNoFuelTypes_shouldIgnoreEmissionZones() {
        accessibilityRequest = AccessibilityRequest.builder().build();
        accessibilityRequest = accessibilityRequest
                .withVehicleWeightInKg(2d)
                .withTransportTypes(Set.of(TransportType.CAR))
                .withTimestamp(timestamp)
                .withFuelTypes(null)
                .withEmissionClasses(Set.of(EmissionClass.EURO_4));

        TransportRestrictions transportConditions = TransportRestrictions.builder()
                .restrictions(TransportConditions.builder().build())
                .exemptions(Collections.emptyList())
                .emissionZone(emissionZone)
                .build();

        assertThat(transportConditions.isRestrictive(accessibilityRequest)).isFalse();
    }

    @Test
    void conditionsApply_emissionZone_requestHasNoEmissionClasses_shouldIgnoreEmissionZones() {
        accessibilityRequest = AccessibilityRequest.builder().build();
        accessibilityRequest = accessibilityRequest
                .withVehicleWeightInKg(2d)
                .withTransportTypes(Set.of(TransportType.CAR))
                .withTimestamp(timestamp)
                .withFuelTypes(Set.of(FuelType.DIESEL))
                .withEmissionClasses(null);

        when(emissionZone.isActive(timestamp)).thenReturn(true);

        TransportRestrictions transportConditions = TransportRestrictions.builder()
                .emissionZone(emissionZone)
                .restrictions(TransportConditions.builder().build())
                .exemptions(Collections.emptyList())
                .build();

        assertThat(transportConditions.isRestrictive(accessibilityRequest)).isFalse();
    }
}
