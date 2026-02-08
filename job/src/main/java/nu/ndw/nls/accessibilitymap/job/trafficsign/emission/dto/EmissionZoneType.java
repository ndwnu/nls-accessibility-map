package nu.ndw.nls.accessibilitymap.job.trafficsign.emission.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EmissionZoneType {
    LOW_EMISSION_ZONE("LOW_EMISSION_ZONE"),
    ZERO_EMISSION_ZONE("ZERO_EMISSION_ZONE"),
    UNKNOWN("UNKNOWN");

    @Getter
    private final String value;

    @JsonCreator
    public static EmissionZoneType fromValue(String value) {

        for (EmissionZoneType type : EmissionZoneType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
