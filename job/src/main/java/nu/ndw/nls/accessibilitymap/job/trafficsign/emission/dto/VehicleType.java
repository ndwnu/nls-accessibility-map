package nu.ndw.nls.accessibilitymap.job.trafficsign.emission.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleType {
    AGRICULTURAL_VEHICLE("AGRICULTURAL_VEHICLE"),
    ANY_VEHICLE("ANY_VEHICLE"),
    ARROW_BOARD_VEHICLE("ARROW_BOARD_VEHICLE"),
    BICYCLE("BICYCLE"),
    BUS("BUS"),
    CAR("CAR"),
    CAR_WITH_CARAVAN("CAR_WITH_CARAVAN"),
    CAR_WITH_TRAILER("CAR_WITH_TRAILER"),
    CONSTRUCTION_OR_MAINTENANCE_VEHICLE("CONSTRUCTION_OR_MAINTENANCE_VEHICLE"),
    CRASH_DAMPENING_VEHICLE("CRASH_DAMPENING_VEHICLE"),
    LORRY("LORRY"),
    MOBILE_LANE_SIGNALING_VEHICLE("MOBILE_LANE_SIGNALING_VEHICLE"),
    MOBILE_VARIABLE_MESSAGE_SIGN_VEHICLE("MOBILE_VARIABLE_MESSAGE_SIGN_VEHICLE"),
    MOPED("MOPED"),
    MOTORCYCLE("MOTORCYCLE"),
    MOTORSCOOTER("MOTORSCOOTER"),
    VAN("VAN"),
    VEHICLE_WITH_TRAILER("VEHICLE_WITH_TRAILER"),
    UNKNOWN("UNKNOWN");

    private final String value;

    @JsonCreator
    public static VehicleType fromValue(String value) {

        for (VehicleType status : VehicleType.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
