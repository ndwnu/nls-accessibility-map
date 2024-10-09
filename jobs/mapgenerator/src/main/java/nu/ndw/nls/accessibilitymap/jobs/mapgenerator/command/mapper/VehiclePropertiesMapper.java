package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties.VehiclePropertiesBuilder;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import org.springframework.stereotype.Component;

@Component
public class VehiclePropertiesMapper {

    public VehicleProperties map(TrafficSignType trafficSignType) {

        VehiclePropertiesBuilder vehiclePropertiesBuilder = VehicleProperties.builder();
        switch (trafficSignType) {
            case C6 -> vehiclePropertiesBuilder.carAccessForbiddenWt(true);
            case C7 -> vehiclePropertiesBuilder.hgvAccessForbiddenWt(true);
            case C7B -> vehiclePropertiesBuilder.hgvAndBusAccessForbiddenWt(true);
            case C12 -> vehiclePropertiesBuilder.motorVehicleAccessForbiddenWt(true);
            case C22C -> vehiclePropertiesBuilder.lcvAndHgvAccessForbiddenWt(true);
            default -> throw new IllegalArgumentException("TrafficSignType is not defined and therefor vehicleProperties could not be created.");
        }

        return vehiclePropertiesBuilder.build();
    }
}
