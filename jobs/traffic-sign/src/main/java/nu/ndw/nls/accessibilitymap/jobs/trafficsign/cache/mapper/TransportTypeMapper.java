package nu.ndw.nls.accessibilitymap.jobs.trafficsign.cache.mapper;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleCategory;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleType;
import org.springframework.stereotype.Component;

@Component
public class TransportTypeMapper {

    public Set<TransportType> map(VehicleType vehicleType, Set<VehicleCategory> vehicleCategories) {

        var transportTypes = mapVehicleType(vehicleType);
        var transportTypesFromCategories = map(vehicleCategories);

        return Stream.concat(transportTypes.stream(), transportTypesFromCategories.stream()).collect(Collectors.toSet());
    }

    public Set<TransportType> map(Set<VehicleCategory> vehicleCategories) {
        if (Objects.isNull(vehicleCategories)) {
            return Set.of();
        }
        return vehicleCategories.stream()
                .map(vehicleCategory ->
                        switch (vehicleCategory) {
                            case M, M_1, M_2, M_3 ->
                                    Set.of(TransportType.BUS, TransportType.CAR, TransportType.TAXI, TransportType.CARAVAN);
                            case N, N_1, N_2, N_3 -> Set.of(TransportType.TRUCK, TransportType.DELIVERY_VAN);
                            case UNKNOWN -> throw new IllegalStateException("Unknown vehicle category '%s'." .formatted(vehicleCategory));
                        })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
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
            case LORRY -> Set.of(TransportType.TRUCK);
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
