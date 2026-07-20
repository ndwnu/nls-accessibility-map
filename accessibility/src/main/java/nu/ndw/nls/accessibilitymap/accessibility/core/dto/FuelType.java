package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FuelType {
    COMPRESSED_NATURAL_GAS("CompressedNaturalGas"),
    DIESEL("Diesel"),
    ETHANOL("Ethanol"),
    ELECTRIC("Electric"),
    HYDROGEN("Hydrogen"),
    LIQUEFIED_PETROLEUM_GAS("LiquefiedPetroleumGas"),
    LIQUEFIED_NATURAL_GAS("LiquefiedNaturalGas"),
    PETROL("Petrol"),
    UNKNOWN("Unknown");

    private final String type;

    @JsonValue
    public String getType() {
        return type;
    }

    @JsonCreator
    public static FuelType fromValue(String value) {
        for (FuelType fuelType : FuelType.values()) {
            if (fuelType.type.equals(value)) {
                return fuelType;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
