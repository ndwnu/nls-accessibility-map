package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.VehicleTypeEnum;
import org.springframework.stereotype.Component;

@Component
public class VehicleToTransportTypeMapper {

    public TransportType map(VehicleTypeEnum vehicleTypeEnum) {
        if (vehicleTypeEnum == null) {
            return null;
        }

        return switch (vehicleTypeEnum) {
            case BICYCLE -> TransportType.BICYCLE;
            case CAR -> TransportType.CAR;
            case BUS -> TransportType.BUS;
            case MOPED -> TransportType.MOPED;
            case MOTORCYCLE -> TransportType.MOTORCYCLE;
            case AGRICULTURAL_VEHICLE -> TransportType.TRACTOR;
            case CARAVAN -> TransportType.CARAVAN;
            case TRAILER -> TransportType.VEHICLE_WITH_TRAILER;
            case MICROCAR -> null;
            case PEDESTRIAN -> TransportType.PEDESTRIAN;
            case TRUCK -> TransportType.TRUCK;
            case DELIVERY_VAN -> TransportType.DELIVERY_VAN;
            case RIDER -> TransportType.RIDERS;
            case TRAM -> TransportType.TRAM;
            case TAXI -> TransportType.TAXI;
            case ALL -> null;
            case UNKNOWN -> null;
            //case ??? -> TransportType.CONDUCTORS
            //case ??? -> TransportType.VEHICLE_WITH_DANGEROUS_SUPPLIES
        };
    }
}
