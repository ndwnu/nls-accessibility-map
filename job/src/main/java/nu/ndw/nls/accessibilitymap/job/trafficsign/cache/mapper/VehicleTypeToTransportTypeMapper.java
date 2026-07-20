package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import java.util.Collections;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.VehicleTypeEnum;
import org.springframework.stereotype.Component;

@Component
public class VehicleTypeToTransportTypeMapper {

    public Set<TransportType> map(VehicleTypeEnum vehicleTypeEnum) {
        if (vehicleTypeEnum == null) {
            return Collections.emptySet();
        }

        return switch (vehicleTypeEnum) {
            case BICYCLE -> Set.of(TransportType.BICYCLE);
            case CAR -> Set.of(TransportType.CAR);
            case BUS -> Set.of(TransportType.BUS);
            case MOPED -> Set.of(TransportType.MOPED);
            case MOTORCYCLE -> Set.of(TransportType.MOTORCYCLE);
            case AGRICULTURAL_VEHICLE -> Set.of(TransportType.TRACTOR);
            case CARAVAN -> Set.of(TransportType.CARAVAN);
            case TRAILER -> Set.of(TransportType.VEHICLE_WITH_TRAILER);
            case MICROCAR -> Set.of(TransportType.CAR);
            case PEDESTRIAN -> Set.of(TransportType.PEDESTRIAN);
            case TRUCK -> Set.of(TransportType.TRUCK);
            case DELIVERY_VAN -> Set.of(TransportType.DELIVERY_VAN);
            case RIDER -> Set.of(TransportType.RIDERS);
            case TRAM -> Set.of(TransportType.TRAM);
            case TAXI -> Set.of(TransportType.TAXI);
            case ALL -> Set.of(TransportType.values());
            case UNKNOWN -> Set.of();
        };
    }
}
