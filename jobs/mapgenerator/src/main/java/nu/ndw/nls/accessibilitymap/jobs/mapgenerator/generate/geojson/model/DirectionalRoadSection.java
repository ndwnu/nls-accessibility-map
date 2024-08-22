package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import lombok.Builder;
import lombok.Value;
import org.locationtech.jts.geom.LineString;

/**
 * Mapped from {@link nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection}, is data in one directrion of the
 * road section
 */
@Value
@Builder
public class DirectionalRoadSection {

    /**
     * This is the original road section id
     */
    long nwbRoadSectionId;

    /**
     * This ID will be negative if the direction is backwards
     */
    long roadSectionId;
    LineString geometry;
    boolean accessible;

    public boolean isForwards() {
        return roadSectionId > 0;
    }

    public boolean isBackwards() {
        return !isForwards();
    }
}
