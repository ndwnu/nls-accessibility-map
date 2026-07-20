package nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmissionZoneType {
    ZERO("ZeroEmissionZone"),
    LOW("LowEmissionZone"),
    UNKNOWN("Unknown");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EmissionZoneType fromValue(String value) {
        for (EmissionZoneType emissionZoneType : EmissionZoneType.values()) {
            if (emissionZoneType.value.equals(value)) {
                return emissionZoneType;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
