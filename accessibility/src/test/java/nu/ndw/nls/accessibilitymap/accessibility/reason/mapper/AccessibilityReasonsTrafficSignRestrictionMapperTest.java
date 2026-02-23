package nu.ndw.nls.accessibilitymap.accessibility.reason.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportRestrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.FuelTypeReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.MaximumReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.TransportTypeReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityReasonsTrafficSignRestrictionMapperTest {

    private AccessibilityReasonsTrafficSignRestrictionMapper mapper;

    @Mock
    private Maximum maximum;

    @BeforeEach
    void setUp() {

        mapper = new AccessibilityReasonsTrafficSignRestrictionMapper();
    }

    @Test
    void mapRestrictions_transportTypeReason() {

        TrafficSign trafficSign = TrafficSign.builder()
                .transportRestrictions(TransportRestrictions.builder()
                        .transportTypes(Set.of(TransportType.CAR))
                        .build())
                .build();

        List<AccessibilityReason<?>> accessibilityReasons = mapper.mapRestrictions(new Restrictions(Set.of(trafficSign)));

        assertThat(accessibilityReasons).hasSize(1);

        assertThat(accessibilityReasons.getFirst()).isInstanceOf(TransportTypeReason.class);
        TransportTypeReason accessibilityReason = (TransportTypeReason) accessibilityReasons.getFirst();

        assertThat(accessibilityReason.getRestrictions()).containsExactly(trafficSign);
        assertThat(accessibilityReason.getValue()).containsExactly(TransportType.CAR);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void mapRestrictions_transportTypeReason_nullOrEmpty(Set<TransportType> transportTypes) {

        TrafficSign trafficSign = TrafficSign.builder()
                .transportRestrictions(TransportRestrictions.builder()
                        .transportTypes(transportTypes)
                        .build())
                .build();

        List<AccessibilityReason<?>> accessibilityReasons = mapper.mapRestrictions(new Restrictions(Set.of(trafficSign)));

        assertThat(accessibilityReasons).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "true",
            "false"
    })
    void mapRestrictions_mapVehicleHeightReason(boolean hasValue) {

        TrafficSign trafficSign = TrafficSign.builder()
                .transportRestrictions(TransportRestrictions.builder()
                        .vehicleHeightInCm(hasValue ? maximum : null)
                        .build())
                .build();

        List<AccessibilityReason<?>> accessibilityReasons = mapper.mapRestrictions(new Restrictions(Set.of(trafficSign)));

        assertMaximumReason(accessibilityReasons, trafficSign, hasValue ? maximum : null);
    }

    @ParameterizedTest
    @CsvSource({
            "true",
            "false"
    })
    void mapRestrictions_mapVehicleWidthReason(boolean hasValue) {

        TrafficSign trafficSign = TrafficSign.builder()
                .transportRestrictions(TransportRestrictions.builder()
                        .vehicleWidthInCm(hasValue ? maximum : null)
                        .build())
                .build();

        List<AccessibilityReason<?>> accessibilityReasons = mapper.mapRestrictions(new Restrictions(Set.of(trafficSign)));

        assertMaximumReason(accessibilityReasons, trafficSign, hasValue ? maximum : null);
    }

    @ParameterizedTest
    @CsvSource({
            "true",
            "false"
    })
    void mapRestrictions_mapVehicleWeightReason(boolean hasValue) {

        TrafficSign trafficSign = TrafficSign.builder()
                .transportRestrictions(TransportRestrictions.builder()
                        .vehicleWeightInKg(hasValue ? maximum : null)
                        .build())
                .build();

        List<AccessibilityReason<?>> accessibilityReasons = mapper.mapRestrictions(new Restrictions(Set.of(trafficSign)));

        assertMaximumReason(accessibilityReasons, trafficSign, hasValue ? maximum : null);
    }

    @ParameterizedTest
    @CsvSource({
            "true",
            "false"
    })
    void mapRestrictions_mapVehicleLengthReason(boolean hasValue) {

        TrafficSign trafficSign = TrafficSign.builder()
                .transportRestrictions(TransportRestrictions.builder()
                        .vehicleLengthInCm(hasValue ? maximum : null)
                        .build())
                .build();

        List<AccessibilityReason<?>> accessibilityReasons = mapper.mapRestrictions(new Restrictions(Set.of(trafficSign)));

        assertMaximumReason(accessibilityReasons, trafficSign, hasValue ? maximum : null);
    }

    @ParameterizedTest
    @CsvSource({
            "true",
            "false"
    })
    void mapRestrictions_mapVehicleAxleLoadReason(boolean hasValue) {

        TrafficSign trafficSign = TrafficSign.builder()
                .transportRestrictions(TransportRestrictions.builder()
                        .vehicleAxleLoadInKg(hasValue ? maximum : null)
                        .build())
                .build();

        List<AccessibilityReason<?>> accessibilityReasons = mapper.mapRestrictions(new Restrictions(Set.of(trafficSign)));

        assertMaximumReason(accessibilityReasons, trafficSign, hasValue ? maximum : null);
    }

    @Test
    void mapRestrictions_emissionZone_vehicleWeightReason() {

        TrafficSign trafficSign = TrafficSign.builder()
                .transportRestrictions(TransportRestrictions.builder()
                        .emissionZone(EmissionZone.builder()
                                .restriction(EmissionZoneRestriction.builder()
                                        .fuelTypes(Set.of())
                                        .transportTypes(Set.of())
                                        .vehicleWeightInKg(maximum)
                                        .build())
                                .build())
                        .build())
                .build();

        List<AccessibilityReason<?>> accessibilityReasons = mapper.mapRestrictions(new Restrictions(Set.of(trafficSign)));

        assertMaximumReason(accessibilityReasons, trafficSign, maximum);
    }

    @Test
    void mapRestrictions_emissionZone_fuelTypeReason() {

        TrafficSign trafficSign = TrafficSign.builder()
                .transportRestrictions(TransportRestrictions.builder()
                        .emissionZone(EmissionZone.builder()
                                .restriction(EmissionZoneRestriction.builder()
                                        .fuelTypes(Set.of(FuelType.DIESEL))
                                        .transportTypes(Set.of())
                                        .vehicleWeightInKg(null)
                                        .build())
                                .build())
                        .build())
                .build();

        List<AccessibilityReason<?>> accessibilityReasons = mapper.mapRestrictions(new Restrictions(Set.of(trafficSign)));

        assertThat(accessibilityReasons).hasSize(1);
        assertThat(accessibilityReasons.getFirst()).isInstanceOf(FuelTypeReason.class);
        FuelTypeReason accessibilityReason = (FuelTypeReason) accessibilityReasons.getFirst();
        assertThat(accessibilityReason.getRestrictions()).containsExactly(trafficSign);
        assertThat(accessibilityReason.getValue()).isEqualTo(Set.of(FuelType.DIESEL));
    }

    @Test
    void mapRestrictions_emissionZone_transportTypeReason() {

        TrafficSign trafficSign = TrafficSign.builder()
                .transportRestrictions(TransportRestrictions.builder()
                        .emissionZone(EmissionZone.builder()
                                .restriction(EmissionZoneRestriction.builder()
                                        .fuelTypes(Set.of())
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .vehicleWeightInKg(null)
                                        .build())
                                .build())
                        .build())
                .build();

        List<AccessibilityReason<?>> accessibilityReasons = mapper.mapRestrictions(new Restrictions(Set.of(trafficSign)));

        assertThat(accessibilityReasons).hasSize(1);
        assertThat(accessibilityReasons.getFirst()).isInstanceOf(TransportTypeReason.class);
        TransportTypeReason accessibilityReason = (TransportTypeReason) accessibilityReasons.getFirst();
        assertThat(accessibilityReason.getRestrictions()).containsExactly(trafficSign);
        assertThat(accessibilityReason.getValue()).isEqualTo(Set.of(TransportType.CAR));
    }

    @Test
    void mapRestrictions_invalidRestrictionType() {

        Restriction restriction = mock(Restriction.class);

        List<AccessibilityReason<?>> accessibilityReasons = mapper.mapRestrictions(new Restrictions(Set.of(restriction)));

        assertThat(accessibilityReasons).isEmpty();
    }

    private static void assertMaximumReason(
            List<AccessibilityReason<?>> accessibilityReasons,
            TrafficSign trafficSign,
            Maximum expectedValue) {

        if (Objects.isNull(expectedValue)) {
            assertThat(accessibilityReasons).isEmpty();
        } else {
            assertThat(accessibilityReasons).hasSize(1);

            assertThat(accessibilityReasons.getFirst()).isInstanceOf(MaximumReason.class);
            MaximumReason accessibilityReason = (MaximumReason) accessibilityReasons.getFirst();

            assertThat(accessibilityReason.getRestrictions()).containsExactly(trafficSign);
            assertThat(accessibilityReason.getValue()).isEqualTo(expectedValue);
        }
    }
}
