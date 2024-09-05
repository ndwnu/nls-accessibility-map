package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional;

public enum Direction {
    FORWARD,
    BACKWARD;

    public boolean isForward() {
        return this == FORWARD;
    }
}
