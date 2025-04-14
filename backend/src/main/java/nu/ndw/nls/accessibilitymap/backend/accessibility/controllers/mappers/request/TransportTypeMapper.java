package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.request;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.VEHICLE_WITH_TRAILER;

import java.util.HashSet;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.VehicleArguments;
import org.springframework.stereotype.Component;

/**
 * Maps information from the {@link VehicleArguments} to a set of {@link TransportType}. It determines the transport types based on the
 * vehicle type and whether the vehicle has a trailer. This class is a Spring component that acts as a utility for converting
 * vehicle-related data to corresponding transport types in the system.
 */
@Component
public class TransportTypeMapper {

    /**
     * Maps the provided {@link VehicleArguments} to a set of {@link TransportType}. It determines the transport types based on the given
     * vehicle type and whether the vehicle has a trailer or not.
     *
     * @param vehicleArguments the input arguments containing details about the vehicle, such as its type and trailer status.
     * @return a set of {@link TransportType} based on the given vehicle arguments.
     */
    public Set<TransportType> mapToTransportType(VehicleArguments vehicleArguments) {
        Set<TransportType> transportTypes = new HashSet<>();
        if (vehicleArguments.vehicleHasTrailer()) {
            transportTypes.add(VEHICLE_WITH_TRAILER);
        }
        switch (vehicleArguments.vehicleType()) {
            case CAR -> transportTypes.add(TransportType.CAR);
            case TRUCK -> transportTypes.add(TransportType.TRUCK);
            case BUS -> transportTypes.add(TransportType.BUS);
            case LIGHT_COMMERCIAL_VEHICLE -> transportTypes.add(TransportType.DELIVERY_VAN);
            case MOTORCYCLE -> transportTypes.add(TransportType.MOTORCYCLE);
            case TRACTOR -> transportTypes.add(TransportType.TRACTOR);
        }
        return transportTypes;
    }
}
