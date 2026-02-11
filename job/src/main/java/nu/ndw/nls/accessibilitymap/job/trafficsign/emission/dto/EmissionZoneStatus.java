package nu.ndw.nls.accessibilitymap.job.trafficsign.emission.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EmissionZoneStatus {
    ACTIVE("ACTIVE"),
    IN_ACTIVE("IN_ACTIVE"),
    BEING_SET_UP("BEING_SET_UP"),
    BEING_SHUT_DOWN("BEING_SHUT_DOWN"),
    SCHEDULED("SCHEDULED"),
    UNKNOWN("UNKNOWN");

    @Getter
    private final String value;

    @JsonCreator
    public static EmissionZoneStatus fromValue(String value) {

        for (EmissionZoneStatus status : EmissionZoneStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
