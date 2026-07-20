package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransportType {

    PEDESTRIAN("Pedestrian"),
    BICYCLE("Bicycle"),
    MOPED("Moped"), //Bromfiets, Snorfiets
    MOTORCYCLE("Motorcycle"),
    CARAVAN("Caravan"),
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
                .filter(transportType -> !excludingTypes.contains(transportType))
                .collect(Collectors.toSet());
    }

    @JsonValue
    public String getType() {
        return type;
    }

    @JsonCreator
    public static TransportType fromValue(String value) {
        for (TransportType transportType : TransportType.values()) {
            if (transportType.type.equals(value)) {
                return transportType;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
