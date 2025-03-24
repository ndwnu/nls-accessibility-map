package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TransportType {

    PEDESTRIAN("Pedestrian"),
    BICYCLE("Bicycle"),
    MOPED("Moped"),
    CAR("Car"),
    TRUCK("Truck"),
    TRACTOR("Tractor"),
    VEHICLE_WITH_TRAILER("VehicleWithTrailer"),
    VEHICLE_WITH_DANGEROUS_SUPPLIES("VehicleWithDangerousSupplies"),
    DELIVERY_VAN("DeliveryVan"),
    RIDERS("Riders"),
    CONDUCTORS("Conductors"),
    BUS("Bus"),
    TRAM("Tram"),
    TAXI("Taxi");

    private final String type;

    @SuppressWarnings("java:S923")
    public static List<TransportType> allExcept(TransportType... excludingTypes) {

        return allExcept(List.of(excludingTypes));
    }

    public static List<TransportType> allExcept(List<TransportType> excludingTypes) {

        return Stream.of(TransportType.values())
                .filter(t -> !excludingTypes.contains(t))
                .toList();
    }
}
