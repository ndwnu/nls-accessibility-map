package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers;


import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;

/**
 * Copy from routing API
 */
@Getter
@RequiredArgsConstructor
public enum TrafficSignApiDrivingDirection {
    FORWARD(DirectionType.FORTH),
    REVERSE(DirectionType.BACK),
    BOTH(DirectionType.BOTH),
    UNKNOWN(null);


    private final DirectionType value;

    public static TrafficSignApiDrivingDirection from(DirectionType value) {
        for (TrafficSignApiDrivingDirection drivingDirection : values()) {
            if (Objects.equals(drivingDirection.getValue(), value)) {
                return drivingDirection;
            }
        }
        return UNKNOWN;
    }

    public boolean isForward() {
        return this == FORWARD || this == BOTH || this == UNKNOWN;
    }

    public boolean isReverse() {
        return this == REVERSE || this == BOTH || this == UNKNOWN;
    }
}
