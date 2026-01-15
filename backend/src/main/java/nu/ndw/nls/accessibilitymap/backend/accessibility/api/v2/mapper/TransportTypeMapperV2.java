package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.VEHICLE_WITH_TRAILER;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.generated.model.v2.VehicleCharacteristicsJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.VehicleTypeJson;
import org.springframework.stereotype.Component;

@Component
public class TransportTypeMapperV2 {

    @SuppressWarnings("java:S1641")
    public Set<TransportType> map(VehicleCharacteristicsJson vehicleCharacteristicsJson) {
        Set<TransportType> transportTypes = new HashSet<>();
        if (Boolean.TRUE.equals(vehicleCharacteristicsJson.getHasTrailer())) {
            transportTypes.add(VEHICLE_WITH_TRAILER);
        }
        transportTypes.add(switch (vehicleCharacteristicsJson.getType()) {
            case VehicleTypeJson.CAR -> TransportType.CAR;
            case VehicleTypeJson.TRUCK -> TransportType.TRUCK;
            case VehicleTypeJson.TRACTOR -> TransportType.TRACTOR;
            case VehicleTypeJson.MOTORCYCLE -> TransportType.MOTORCYCLE;
            case VehicleTypeJson.LIGHT_COMMERCIAL_VEHICLE -> TransportType.DELIVERY_VAN;
            case VehicleTypeJson.BUS -> TransportType.BUS;
        });
        return transportTypes;
    }

    public List<VehicleTypeJson> map(Set<TransportType> transportTypeSet) {
        return transportTypeSet.stream()
                .map(transportType -> switch (transportType) {
                    case TransportType.CAR -> VehicleTypeJson.CAR;
                    case TransportType.TRUCK -> VehicleTypeJson.TRUCK;
                    case TransportType.TRACTOR -> VehicleTypeJson.TRACTOR;
                    case TransportType.MOTORCYCLE -> VehicleTypeJson.MOTORCYCLE;
                    case TransportType.DELIVERY_VAN -> VehicleTypeJson.LIGHT_COMMERCIAL_VEHICLE;
                    case TransportType.BUS -> VehicleTypeJson.BUS;
                    default -> null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
