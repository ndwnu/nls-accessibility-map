package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional;

import lombok.Builder;
import lombok.Value;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;

/**
 * Directional traffic signs are mapped from {@link TrafficSignGeoJsonDto} and the fraction property is adjusted to the
 * driving direction. It is used along with {@link DirectionalRoadSection} as an intermediate result that can be used to
 * map it to our GeoJson end result.
 */

@Value
@Builder
public class DirectionalTrafficSign {

    Direction direction;

    long nwbRoadSectionId;

    double nwbFraction;

    TrafficSignType trafficSignType;

    String windowTimes;

    /**
     * Returns the fraction in driving direction
     */
    public double getFraction() {
        if (direction == Direction.FORWARD) {
            return nwbFraction;
        } else {
            return 1-getNwbFraction();
        }
    }

}
