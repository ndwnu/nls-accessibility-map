package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;

public interface Properties {

    long getNwbRoadSectionId();

    boolean isAccessible();

    Direction getDirection();
}
