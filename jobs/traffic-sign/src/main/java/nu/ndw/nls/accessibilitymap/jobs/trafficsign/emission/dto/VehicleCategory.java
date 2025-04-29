package nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Official definition can be found here:
 * <a href="https://alternative-fuels-observatory.ec.europa.eu/general-information/vehicle-types">...</a>
 */
@Getter
@RequiredArgsConstructor
public enum VehicleCategory {
    //Power-driven vehicles having at least four wheels and used for the carriage of passengers.
    M("M"),

    //Vehicles used for the carriage of passengers and comprising not more than eight seats in addition to the driver's seat.
    M_1("M_1"),

    //Vehicles used for the carriage of passengers, comprising more than eight seats in addition to the driver's seat, and having a maximum mass not exceeding 5 tonnes.
    M_2("M_2"),

    //Vehicles used for the carriage of passengers, comprising more than eight seats in addition to the driver's seat, and having a maximum mass exceeding 5 tonnes.
    M_3("M_3"),

    //Power-driven vehicles having at least four wheels and used for the carriage of goods
    N("N"),

    //Vehicles used for the carriage of goods and having a maximum mass not exceeding 3.5 tonnes.
    N_1("N_1"),

    //Vehicles used for the carriage of goods and having a maximum mass exceeding 3.5 tonnes but not exceeding 12 tonnes.
    N_2("N_2"),

    //Vehicles used for the carriage of goods and having a maximum mass exceeding 12 tonnes.
    N_3("N_3"),

    UNKNOWN("UNKNOWN");

    private final String value;

    @JsonCreator
    public static VehicleCategory fromValue(String value) {

        for (VehicleCategory status : VehicleCategory.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
