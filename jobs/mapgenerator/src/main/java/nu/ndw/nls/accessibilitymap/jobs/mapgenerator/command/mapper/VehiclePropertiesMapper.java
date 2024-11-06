package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.mapper;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties.VehiclePropertiesBuilder;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import org.springframework.stereotype.Component;

@Component
public class VehiclePropertiesMapper {

    public VehicleProperties map(List<TrafficSignType> trafficSignTypes, boolean includeOnlyWindowTimes) {

        VehiclePropertiesBuilder vehiclePropertiesBuilder = VehicleProperties.builder();
        for (TrafficSignType trafficSignType : trafficSignTypes) {
            switch (trafficSignType) {
                case C6 -> {
                    vehiclePropertiesBuilder.carAccessForbiddenWt(true);
                    if (!includeOnlyWindowTimes) {
                        vehiclePropertiesBuilder.carAccessForbidden(true);
                    }
                }
                case C7 -> {
                    vehiclePropertiesBuilder.hgvAccessForbiddenWt(true);
                    if (!includeOnlyWindowTimes) {
                        vehiclePropertiesBuilder.hgvAccessForbidden(true);
                    }
                }
                case C7B -> {
                    vehiclePropertiesBuilder.hgvAndBusAccessForbiddenWt(true);
                    if (!includeOnlyWindowTimes) {
                        vehiclePropertiesBuilder.hgvAndBusAccessForbidden(true);
                    }
                }
                case C12 -> {
                    vehiclePropertiesBuilder.motorVehicleAccessForbiddenWt(true);
                    if (!includeOnlyWindowTimes) {
                        vehiclePropertiesBuilder.motorVehicleAccessForbidden(true);
                    }
                }
                case C22C -> {
                    vehiclePropertiesBuilder.lcvAndHgvAccessForbiddenWt(true);
                    if (!includeOnlyWindowTimes) {
                        vehiclePropertiesBuilder.lcvAndHgvAccessForbidden(true);
                    }
                }
                default -> throw new IllegalArgumentException(
                        "TrafficSignType is not defined and therefor vehicleProperties could not be created.");
            }
        }
        return vehiclePropertiesBuilder.build();
    }
}
