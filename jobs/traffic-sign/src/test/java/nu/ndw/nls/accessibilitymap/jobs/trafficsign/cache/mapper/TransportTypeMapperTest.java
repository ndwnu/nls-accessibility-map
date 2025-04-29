package nu.ndw.nls.accessibilitymap.jobs.trafficsign.cache.mapper;

import static nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleCategory.M;
import static nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleCategory.M_1;
import static nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleCategory.M_2;
import static nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleCategory.M_3;
import static nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleCategory.N;
import static nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleCategory.N_1;
import static nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleCategory.N_2;
import static nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleCategory.N_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleCategory;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransportTypeMapperTest {

    private TransportTypeMapper transportTypeMapper;

    @BeforeEach
    void setUp() {

        transportTypeMapper = new TransportTypeMapper();
    }

    @ParameterizedTest
    @EnumSource(VehicleType.class)
    @NullSource
    void map_vehicleType(VehicleType vehicleType) {

        if (vehicleType == VehicleType.UNKNOWN) {
            assertThat(catchThrowable(() -> transportTypeMapper.map(vehicleType, null)))
                    .withFailMessage("Unknown vehicle type '%s'." .formatted(vehicleType))
                    .isInstanceOf(IllegalStateException.class);
        } else {

            Set<TransportType> transportTypes = transportTypeMapper.map(vehicleType, null);

            if (Objects.isNull(vehicleType)) {
                assertThat(transportTypes).isEmpty();
            } else {
                assertThat(transportTypes).containsExactlyInAnyOrderElementsOf(mapVehicleType(vehicleType));
            }
        }
    }

    @Test
    void map_vehicleTypeAndVehicleCategory() {
        Set<TransportType> transportTypes = transportTypeMapper.map(VehicleType.CAR, Set.of(N_1));

        assertThat(transportTypes).containsExactlyInAnyOrder(TransportType.CAR, TransportType.DELIVERY_VAN, TransportType.TRUCK);
    }

    @ParameterizedTest
    @EnumSource(VehicleCategory.class)
    @NullSource
    void map_vehicleCategory(VehicleCategory vehicleCategory) {

        if (vehicleCategory == VehicleCategory.UNKNOWN) {
            assertThat(catchThrowable(() -> transportTypeMapper.map(Set.of(vehicleCategory))))
                    .withFailMessage("Unknown vehicle category '%s'." .formatted(vehicleCategory))
                    .isInstanceOf(IllegalStateException.class);
        } else {

            Set<TransportType> transportTypes = transportTypeMapper.map(Objects.nonNull(vehicleCategory) ? Set.of(vehicleCategory) : null);

            if (Objects.isNull(vehicleCategory)) {
                assertThat(transportTypes).isEmpty();
            } else if (List.of(M, M_1, M_2, M_3).contains(vehicleCategory)) {
                assertThat(transportTypes)
                        .isEqualTo(Set.of(TransportType.BUS, TransportType.CAR, TransportType.TAXI, TransportType.CARAVAN));
            } else if (List.of(N, N_1, N_2, N_3).contains(vehicleCategory)) {
                assertThat(transportTypes)
                        .isEqualTo(Set.of(TransportType.TRUCK, TransportType.DELIVERY_VAN));
            } else {
                assertThat(transportTypes).isEmpty();
            }
        }
    }

    private static Set<TransportType> mapVehicleType(VehicleType vehicleType) {

        if (vehicleType == null) {
            return Set.of();
        }

        return switch (vehicleType) {
            case AGRICULTURAL_VEHICLE -> Set.of(TransportType.TRACTOR);
            case ANY_VEHICLE -> Set.of(TransportType.values());
            case BICYCLE -> Set.of(TransportType.BICYCLE);
            case BUS -> Set.of(TransportType.BUS);
            case CAR -> Set.of(TransportType.CAR);
            case CAR_WITH_CARAVAN -> Set.of(TransportType.CARAVAN);
            case CAR_WITH_TRAILER -> Set.of(TransportType.CAR, TransportType.VEHICLE_WITH_TRAILER);
            case LORRY  -> Set.of(TransportType.TRUCK);
            case VAN -> Set.of(TransportType.DELIVERY_VAN);
            case MOPED, MOTORSCOOTER -> Set.of(TransportType.MOPED);
            case MOTORCYCLE -> Set.of(TransportType.MOTORCYCLE);
            case VEHICLE_WITH_TRAILER -> Set.of(TransportType.VEHICLE_WITH_TRAILER);
            case ARROW_BOARD_VEHICLE, CONSTRUCTION_OR_MAINTENANCE_VEHICLE, CRASH_DAMPENING_VEHICLE, MOBILE_VARIABLE_MESSAGE_SIGN_VEHICLE,
                 MOBILE_LANE_SIGNALING_VEHICLE -> Set.of();
            case UNKNOWN -> throw new IllegalStateException("Unknown vehicle type '%s'." .formatted(vehicleType));
        };
    }

}