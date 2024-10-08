package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model;

public enum Direction {
    FORWARD,
    BACKWARD;

    public boolean isForward() {
        return this == FORWARD;
    }

    public boolean isBackward() {
        return this == BACKWARD;
    }
}
