package nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FuelType {
    ALL("ALL"),
    BATTERY("BATTERY"),
    BIODIESEL("BIODIESEL"),
    DIESEL("DIESEL"),
    DIESEL_BATTERY_HYBRID("DIESEL_BATTERY_HYBRID"),
    ETHANOL("ETHANOL"),
    HYDROGEN("HYDROGEN"),
    LIQUID_GAS("LIQUID_GAS"),
    LPG("LPG"),
    METHANE("METHANE"),
    OTHER("OTHER"),
    PETROL("PETROL"),
    PETROL_95_OCTANE("PETROL_95_OCTANE"),
    PETROL_98_OCTANE("PETROL_98_OCTANE"),
    PETROL_BATTERY_HYBRID("PETROL_BATTERY_HYBRID"),
    PETROL_LEADED("PETROL_LEADED"),
    PETROL_UNLEADED("PETROL_UNLEADED"),
    UNKNOWN("UNKNOWN");

    private final String value;

    @JsonCreator
    public static FuelType fromValue(String value) {

        for (FuelType status : FuelType.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}