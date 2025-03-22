package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.dto.value;

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
    BUS("Bus"),
    TRAM("Tram"),
    TAXI("Taxi"),
    OTHER("Other");

    private final String type;
}
