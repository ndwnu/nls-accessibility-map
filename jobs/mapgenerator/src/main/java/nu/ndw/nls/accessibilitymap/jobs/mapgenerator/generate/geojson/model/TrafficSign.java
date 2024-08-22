package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Holds the intermediate result
 */
@Data
@Builder
@RequiredArgsConstructor
public class TrafficSign {
    private final long roadSectionId;
    private final TrafficSignType trafficSignType;
    private final String windowTimes;
}
