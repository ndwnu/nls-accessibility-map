package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.VEHICLE_WITH_TRAILER;
import static nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeJson.BUS;
import static nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeJson.CAR;
import static nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeJson.LIGHT_COMMERCIAL_VEHICLE;
import static nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeJson.MOTORCYCLE;
import static nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeJson.TRACTOR;
import static nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeJson.TRUCK;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeJson;
import org.springframework.stereotype.Component;

/**
 * Maps information from the {@link VehicleArguments} to a set of {@link TransportType}. It determines the transport types based on the
 * vehicle type and whether the vehicle has a trailer. This class is a Spring component that acts as a utility for converting
 * vehicle-related data to corresponding transport types in the system.
 */
@Component
public class TransportTypeMapper {

    private static final Map<VehicleTypeJson, TransportType> vehicleTypeToTransportTypeMap = Map.of(
            CAR, TransportType.CAR,
            TRUCK, TransportType.TRUCK,
            BUS, TransportType.BUS,
            LIGHT_COMMERCIAL_VEHICLE, TransportType.DELIVERY_VAN,
            MOTORCYCLE, TransportType.MOTORCYCLE,
            TRACTOR, TransportType.TRACTOR
    );

    private static final Map<TransportType, VehicleTypeJson> transportTypeToVehicleTypeMap = vehicleTypeToTransportTypeMap.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    /**
     * Maps the provided {@link VehicleArguments} to a set of {@link TransportType}. It determines the transport types based on the given
     * vehicle type and whether the vehicle has a trailer or not.
     *
     * @param vehicleArguments the input arguments containing details about the vehicle, such as its type and trailer status.
     * @return a set of {@link TransportType} based on the given vehicle arguments.
     */
    @SuppressWarnings("java:S1641")
    public Set<TransportType> map(VehicleArguments vehicleArguments) {
        Set<TransportType> transportTypes = new HashSet<>();
        if (Boolean.TRUE.equals(vehicleArguments.vehicleHasTrailer())) {
            transportTypes.add(VEHICLE_WITH_TRAILER);
        }
        if (vehicleTypeToTransportTypeMap.containsKey(vehicleArguments.vehicleType())) {
            transportTypes.add(vehicleTypeToTransportTypeMap.get(vehicleArguments.vehicleType()));
        }
        return transportTypes;
    }

    public List<VehicleTypeJson> map(Set<TransportType> transportTypeSet) {
        return transportTypeSet.stream()
                .filter(transportTypeToVehicleTypeMap::containsKey)
                .map(transportTypeToVehicleTypeMap::get)
                .toList();
    }
}
