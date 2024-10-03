package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

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
