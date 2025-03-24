package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import java.util.Set;
import java.util.stream.Collectors;
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
    public static Set<TransportType> allExcept(TransportType... excludingTypes) {

        return allExcept(Set.of(excludingTypes));
    }

    public static Set<TransportType> allExcept(Set<TransportType> excludingTypes) {

        return Stream.of(TransportType.values())
                .filter(t -> !excludingTypes.contains(t))
                .collect(Collectors.toSet());
    }
}
