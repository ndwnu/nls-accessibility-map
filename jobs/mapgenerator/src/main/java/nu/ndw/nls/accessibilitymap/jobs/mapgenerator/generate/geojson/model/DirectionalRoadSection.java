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
    long roadSectionId;
    LineString geometry;
    boolean accessible;
}
