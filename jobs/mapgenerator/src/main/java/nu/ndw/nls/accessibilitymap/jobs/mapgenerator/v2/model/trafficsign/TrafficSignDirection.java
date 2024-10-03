package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign;

public enum TrafficSignDirection {
    FORWARD,
    BACKWARD,
    BOTH;

    public boolean isForward() {
        return this == FORWARD || this == BOTH;
    }

    public boolean isBackward() {
        return this == BACKWARD || this == BOTH;
    }
}
