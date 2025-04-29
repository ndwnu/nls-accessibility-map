package nu.ndw.nls.accessibilitymap.jobs.trafficsign.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.cache.mapper.EmissionZoneMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignRestrictionsBuilderTest {

    private TrafficSignRestrictionsBuilder trafficSignRestrictionsBuilder;

    @Mock
    private EmissionZoneMapper emissionZoneMapper;

    @Mock
    private EmissionZone emissionZone;

    @BeforeEach
    void setUp() {

        trafficSignRestrictionsBuilder = new TrafficSignRestrictionsBuilder(emissionZoneMapper);
    }

    @ParameterizedTest
    @EnumSource(TrafficSignType.class)
    void buildFor_allTrafficSignTypesCanBeMapped(TrafficSignType trafficSignType) {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(trafficSignType)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isNotNull();
    }

    @Test
    void buildFor_noTrafficSignType() {

        TrafficSign trafficSign = TrafficSign.builder().build();

        assertThat(catchThrowable(() -> trafficSignRestrictionsBuilder.buildFor(trafficSign)))
                .withFailMessage("Traffic sign type null is not supported")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buildFor_c1() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C1)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .transportTypes(Arrays.stream(TransportType.values())
                        .filter(transportType -> transportType != TransportType.PEDESTRIAN)
                        .collect(Collectors.toSet()))
                .build());
    }

    @Test
    void buildFor_c6() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C6)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .transportTypes(Set.of(
                        TransportType.BUS,
                        TransportType.CAR,
                        TransportType.DELIVERY_VAN,
                        TransportType.TAXI,
                        TransportType.TRACTOR,
                        TransportType.TRUCK
                ))
                .build());
    }

    @Test
    void buildFor_c7() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C7)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .transportTypes(Set.of(TransportType.TRUCK))
                .build());
    }

    @Test
    void buildFor_c7a() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C7A)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .transportTypes(Set.of(TransportType.BUS))
                .build());
    }

    @Test
    void buildFor_c7b() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C7B)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .transportTypes(Set.of(TransportType.BUS, TransportType.TRUCK))
                .build());
    }

    @Test
    void buildFor_c7c() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C7C)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .transportTypes(Set.of(TransportType.DELIVERY_VAN, TransportType.TRUCK))
                .build());
    }

    @Test
    void buildFor_c10() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C10)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .transportTypes(Set.of(
                        TransportType.VEHICLE_WITH_TRAILER
                ))
                .build());
    }

    @Test
    void buildFor_c11() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C11)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .transportTypes(Set.of(
                        TransportType.MOTORCYCLE
                ))
                .build());
    }

    @Test
    void buildFor_c12() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C12)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .transportTypes(Set.of(
                        TransportType.BUS,
                        TransportType.CAR,
                        TransportType.DELIVERY_VAN,
                        TransportType.MOPED,
                        TransportType.MOTORCYCLE,
                        TransportType.MOTORSCOOTER,
                        TransportType.TAXI,
                        TransportType.TRACTOR,
                        TransportType.TRUCK
                ))
                .build());
    }

    @Test
    void buildFor_c17() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C17)
                .blackCode(10d)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .vehicleLengthInCm(Maximum.builder().value(1_000d).build())
                .build());
    }

    @Test
    void buildFor_c18() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C18)
                .blackCode(10d)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .vehicleWidthInCm(Maximum.builder().value(1_000d).build())
                .build());
    }

    @Test
    void buildFor_c19() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C19)
                .blackCode(10d)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .vehicleHeightInCm(Maximum.builder().value(1_000d).build())
                .build());
    }

    @Test
    void buildFor_c20() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C20)
                .blackCode(10d)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .vehicleAxleLoadInKg(Maximum.builder().value(10_000d).build())
                .build());
    }

    @Test
    void buildFor_c21() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C21)
                .blackCode(10d)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .vehicleWeightInKg(Maximum.builder().value(10_000d).build())
                .build());
    }

    @Test
    void buildFor_c22() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C22)
                .build();

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .transportTypes(Set.of(TransportType.VEHICLE_WITH_DANGEROUS_SUPPLIES))
                .build());
    }

    @Test
    void buildFor_c22A() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C22A)
                .build();

        when(emissionZoneMapper.map(trafficSign.emissionZoneId())).thenReturn(emissionZone);

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .emissionZone(emissionZone)
                .build());
    }

    @Test
    void buildFor_c22C() {

        TrafficSign trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C22C)
                .build();

        when(emissionZoneMapper.map(trafficSign.emissionZoneId())).thenReturn(emissionZone);

        assertThat(trafficSignRestrictionsBuilder.buildFor(trafficSign)).isEqualTo(Restrictions.builder()
                .emissionZone(emissionZone)
                .build());
    }
}
