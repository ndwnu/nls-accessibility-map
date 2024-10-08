package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.Direction;

public interface Properties {

    long getNwbRoadSectionId();

    boolean isAccessible();

    Direction getDirection();
}
