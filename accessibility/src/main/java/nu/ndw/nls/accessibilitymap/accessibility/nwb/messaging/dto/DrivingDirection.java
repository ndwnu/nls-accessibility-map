package nu.ndw.nls.accessibilitymap.accessibility.nwb.messaging.dto;

public enum DrivingDirection {
    BACK, FORTH, BOTH;

    public boolean isForwardAccessible() {
        return this != BACK;
    }

    public boolean isBackwardAccessible() {
        return this != FORTH;
    }
}
