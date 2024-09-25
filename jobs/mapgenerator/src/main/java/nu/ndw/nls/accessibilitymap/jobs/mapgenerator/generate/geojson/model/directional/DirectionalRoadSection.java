package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityMapService.ResultType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import org.locationtech.jts.geom.LineString;

/**
 * Mapped from {@link nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection}, is data in one driving direction
 * of the road section. To make mapping easier, there are two ways of obtaining the geometry.
 * {@link DirectionalRoadSection#nwbGeometry} is the original NWB geometry and
 * {@link DirectionalRoadSection#getGeometry()} is reversed if this is the backward direction. It is used along with
 * {@link DirectionalTrafficSign} as an intermediate result that can be used to map it to our GeoJson end result. No
 * logic in DTOs is preferred, but in this case it's convenient to use a little bit of logic to always return the
 * geometry in the driving direction. Because we reverse the geometry on demand, this also saves memory compared to
 * having the reverse geometry mapped into this DTO.
 */
@Value
@With
@Builder
public class DirectionalRoadSection {

    Direction direction;

    /**
     * This is the original road section id
     */
    long nwbRoadSectionId;

    /**
     * The original nwb geometry
     */
    LineString nwbGeometry;

    /**
     * Depending on {@link ResultType#DIFFERENCE_OF_ADDED_RESTRICTIONS} this can either
     * {@link ResultType#DIFFERENCE_OF_ADDED_RESTRICTIONS} never null, all NWB road that exists in the database
     *          are always initialized to true values and set to false if inaccessible
     * {@link ResultType#EFFECTIVE_ACCESSIBILITY} could be null if not accessible to begin with in the unrestricted
     *          isochrone, for example if the road section is isolated by not having any connected roads
     */
    Boolean accessible;

    /**
     * @return geometry in driving direction, which means it returns the reverse if the driving direction is backwards.
     */
    public LineString getGeometry() {
        if (direction == Direction.FORWARD) {
            return nwbGeometry;
        } else {
            return nwbGeometry.reverse();
        }
    }

}
