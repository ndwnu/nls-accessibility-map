package nu.ndw.nls.accessibilitymap.backend.mappers;

import java.util.EnumMap;
import java.util.function.Consumer;
import nu.ndw.nls.accessibilitymap.backend.controllers.AccessibilityMapApiDelegateImpl.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.exceptions.VehicleTypeNotSupportedException;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties.VehiclePropertiesBuilder;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    private static final String MESSAGE_TEMPLATE = "The vehicle type: %s is not supported";
    private static final EnumMap<VehicleTypeJson, Consumer<VehiclePropertiesBuilder>>
            VEHICLE_TYPE_CONFIGURATION = new EnumMap<>(VehicleTypeJson.class);

    static {
        VEHICLE_TYPE_CONFIGURATION.put(VehicleTypeJson.BUS, builder -> builder
                .motorVehicleAccessForbidden(true)
                .carAccessForbidden(true)
                .busAccessForbidden(true)
                .hgvAndBusAccessForbidden(true));

        VEHICLE_TYPE_CONFIGURATION.put(VehicleTypeJson.CAR, builder -> builder
                .motorVehicleAccessForbidden(true)
                .carAccessForbidden(true));

        VEHICLE_TYPE_CONFIGURATION.put(VehicleTypeJson.LIGHT_COMMERCIAL_VEHICLE, builder -> builder
                .motorVehicleAccessForbidden(true)
                .carAccessForbidden(true)
                .lcvAndHgvAccessForbidden(true));

        VEHICLE_TYPE_CONFIGURATION.put(VehicleTypeJson.MOTORCYCLE, builder -> builder
                .motorVehicleAccessForbidden(true)
                .motorcycleAccessForbidden(true));

        VEHICLE_TYPE_CONFIGURATION.put(VehicleTypeJson.TRACTOR, builder -> builder
                .motorVehicleAccessForbidden(true)
                .carAccessForbidden(true)
                .tractorAccessForbidden(true)
                .slowVehicleAccessForbidden(true));

        VEHICLE_TYPE_CONFIGURATION.put(VehicleTypeJson.TRUCK, builder -> builder
                .motorVehicleAccessForbidden(true)
                .carAccessForbidden(true)
                .hgvAccessForbidden(true)
                .hgvAndBusAccessForbidden(true)
                .lcvAndHgvAccessForbidden(true));
    }

    public VehicleProperties mapToVehicleProperties(VehicleArguments requestArguments) {
        if (!VEHICLE_TYPE_CONFIGURATION.containsKey(requestArguments.vehicleType())) {
            throw new VehicleTypeNotSupportedException(MESSAGE_TEMPLATE.formatted(requestArguments.vehicleType()));
        }

        VehiclePropertiesBuilder vehiclePropertiesBuilder = VehicleProperties.builder();
        applyVehiclePropertiesForVehicleType(vehiclePropertiesBuilder, requestArguments.vehicleType());
        vehiclePropertiesBuilder
                .trailerAccessForbidden(requestArguments.vehicleHasTrailer() == Boolean.TRUE)
                .height(convertToDouble(requestArguments.vehicleHeight()))
                .width(convertToDouble(requestArguments.vehicleWidth()))
                .axleLoad(convertToDouble(requestArguments.vehicleAxleLoad()))
                .length(convertToDouble(requestArguments.vehicleLength()))
                .weight(convertToDouble(requestArguments.vehicleWeight()));

        return vehiclePropertiesBuilder.build();
    }

    private void applyVehiclePropertiesForVehicleType(
            VehiclePropertiesBuilder vehiclePropertiesBuilder,
            VehicleTypeJson vehicledType
    ) {
        VEHICLE_TYPE_CONFIGURATION.get(vehicledType).accept(vehiclePropertiesBuilder);
    }

    private static Double convertToDouble(Float floatValue) {
        return floatValue != null ? Double.valueOf(floatValue) : null;
    }
}
