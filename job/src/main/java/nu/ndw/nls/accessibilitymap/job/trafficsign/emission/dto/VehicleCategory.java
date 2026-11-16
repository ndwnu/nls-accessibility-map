package nu.ndw.nls.accessibilitymap.job.trafficsign.emission.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Official definition can be found here:
 * <a href="https://www.rdw.nl/paginas/voertuigcategorieen">...</a>
 */
@Getter
@RequiredArgsConstructor
public enum VehicleCategory {
    /*
        Motor vehicle with 4 or more wheels, designed and constructed for the carriage of passengers.
     */
    M("M"),

    /*
        Passenger car (personenauto). Motor vehicle with 4 or more wheels, designed and constructed for the carriage of passengers,
        with a maximum of 9 seats including the driver's seat.
     */
    M_1("M_1"),

    /*
        Bus. Motor vehicle with 4 or more wheels, designed and constructed for the carriage of passengers, with more than 9 seats
        including the driver's seat.
     */
    M_2("M_2"),

    /*
        Bus. Motor vehicle with 4 or more wheels, designed and constructed for the carriage of passengers, with more than 9 seats
        including the driver's seat.
     */
    M_3("M_3"),

    /*
        Motor vehicle with 4 or more wheels, designed and constructed for the carriage of goods.
     */
    N("N"),

    /*
        Light commercial vehicle (lichte bedrijfsauto), lighter than 3500 kg (F.1 on the registration certificate). Motor vehicle with
        4 or more wheels, designed and constructed for the carriage of goods.
     */
    N_1("N_1"),

    /*
        Heavy commercial vehicle (zware bedrijfsauto), heavier than 3500 kg (F.1 on the registration certificate). Motor vehicle with
        4 or more wheels, designed and constructed for the carriage of goods.
     */
    N_2("N_2"),

    /*
        Heavy commercial vehicle (zware bedrijfsauto), heavier than 3500 kg (F.1 on the registration certificate). Motor vehicle with
        4 or more wheels, designed and constructed for the carriage of goods.
     */
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
