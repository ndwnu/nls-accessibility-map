package nu.ndw.nls.accessibilitymap.backend.mappers;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.VEHICLE_WITH_TRAILER;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.backend.controllers.dto.VehicleArguments;
import org.springframework.stereotype.Component;

@Component
public class TransportTypeV2Mapper {

       public Set<TransportType> mapToTransportType(VehicleArguments vehicleArguments) {
           Set<TransportType> transportTypes = new HashSet<>();
           if(vehicleArguments.vehicleHasTrailer()){
               transportTypes.add(VEHICLE_WITH_TRAILER);
           }
           switch (vehicleArguments.vehicleType()) {
               case CAR -> transportTypes.add(TransportType.CAR);
               case TRUCK -> transportTypes.add(TransportType.TRUCK);
               case BUS -> transportTypes.add(TransportType.BUS);
               case LIGHT_COMMERCIAL_VEHICLE -> transportTypes.add(TransportType.DELIVERY_VAN);
               case MOTORCYCLE ->  transportTypes.add(TransportType.MOTORCYCLE);
               case TRACTOR -> transportTypes.add(TransportType.TRACTOR);
           }
           return transportTypes;
       }
}
