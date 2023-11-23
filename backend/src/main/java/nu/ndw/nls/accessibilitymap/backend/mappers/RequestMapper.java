package nu.ndw.nls.accessibilitymap.backend.mappers;

import java.util.EnumMap;
import java.util.function.BiConsumer;
import nu.ndw.nls.accessibilitymap.backend.controllers.AccessibilityMapApiDelegateImpl.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.exceptions.VehicleTypeNotSupportedException;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.routingmapmatcher.domain.model.accessibility.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.domain.model.accessibility.VehicleProperties.VehiclePropertiesBuilder;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    private static final String MESSAGE_TEMPLATE = "The vehicle type: %s is not supported";
    private static final EnumMap<VehicleTypeJson, BiConsumer<VehiclePropertiesBuilder, Float>>
            VEHICLE_TYPE_CONFIGURATION = new EnumMap<>(VehicleTypeJson.class);

    private static final double HGV_WEIGHT_IN_TONNES = 3.5;

    static {
        VEHICLE_TYPE_CONFIGURATION.put(VehicleTypeJson.CAR, (builder, weight) -> builder
                .motorVehicleAccessForbidden(true)
                .carAccessForbidden(true));

        VEHICLE_TYPE_CONFIGURATION.put(VehicleTypeJson.COMMERCIAL_VEHICLE, (builder, weight) -> builder
                .motorVehicleAccessForbidden(true)
                .carAccessForbidden(true)
                .hgvAccessForbidden(weight > HGV_WEIGHT_IN_TONNES)
                .hgvAndBusAccessForbidden(weight > HGV_WEIGHT_IN_TONNES)
                .lcvAndHgvAccessForbidden(true));

        VEHICLE_TYPE_CONFIGURATION.put(VehicleTypeJson.BUS, (builder, weight) -> builder
                .motorVehicleAccessForbidden(true)
                .carAccessForbidden(true)
                .busAccessForbidden(true)
                .hgvAndBusAccessForbidden(true));

        VEHICLE_TYPE_CONFIGURATION.put(VehicleTypeJson.TRACTOR, (builder, weight) -> builder
                .motorVehicleAccessForbidden(true)
                .carAccessForbidden(true)
                .tractorAccessForbidden(true)
                .slowVehicleAccessForbidden(true));

        VEHICLE_TYPE_CONFIGURATION.put(VehicleTypeJson.MOTORCYCLE, (builder, weight) -> builder
                .motorVehicleAccessForbidden(true)
                .motorcycleAccessForbidden(true));
    }

    public VehicleProperties mapToVehicleProperties(VehicleArguments requestArguments) {
        if (!VEHICLE_TYPE_CONFIGURATION.containsKey(requestArguments.vehicleType())) {
            throw new VehicleTypeNotSupportedException(String.format(MESSAGE_TEMPLATE, requestArguments.vehicleType()));
        }

        BiConsumer<VehiclePropertiesBuilder, Float> vehiclePropertiesConfiguration = VEHICLE_TYPE_CONFIGURATION.get(
                requestArguments.vehicleType());
        VehiclePropertiesBuilder vehiclePropertiesBuilder = VehicleProperties.builder();
        vehiclePropertiesConfiguration.accept(vehiclePropertiesBuilder, requestArguments.vehicleWeight());
        vehiclePropertiesBuilder
                .trailerAccessForbidden(requestArguments.vehicleHasTrailer())
                .height(convertToDouble(requestArguments.vehicleHeight()))
                .width(convertToDouble(requestArguments.vehicleWidth()))
                .axleLoad(convertToDouble(requestArguments.vehicleAxleLoad()))
                .length(convertToDouble(requestArguments.vehicleLength()))
                .weight(convertToDouble(requestArguments.vehicleWeight()));

        return vehiclePropertiesBuilder.build();
    }

    private static Double convertToDouble(Float floatValue) {
        return floatValue != null ? Double.valueOf(floatValue) : null;
    }
}
