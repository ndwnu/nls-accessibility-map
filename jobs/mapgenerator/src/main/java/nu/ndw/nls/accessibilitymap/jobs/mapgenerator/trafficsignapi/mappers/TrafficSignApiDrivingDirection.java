package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers;


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
    BOTH(DirectionType.BOTH);


    private final DirectionType value;

    public static TrafficSignApiDrivingDirection from(DirectionType value) {
        for (TrafficSignApiDrivingDirection drivingDirection : values()) {
            if (drivingDirection.getValue().equals(value)) {
                return drivingDirection;
            }
        }
        throw new IllegalArgumentException("Unexpected driving direction: " + value);
    }

    public boolean isForward() {
        return this == FORWARD || this == BOTH;
    }

    public boolean isReverse() {
        return this == REVERSE || this == BOTH;
    }
}
