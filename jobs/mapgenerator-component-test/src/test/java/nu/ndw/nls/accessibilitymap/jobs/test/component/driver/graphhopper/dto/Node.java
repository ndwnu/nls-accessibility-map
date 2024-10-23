package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto;

import lombok.Builder;
import org.locationtech.jts.geom.Coordinate;

@Builder
public record Node(
        long id,
        Coordinate coordinate) {
}
