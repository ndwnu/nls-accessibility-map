package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto;

import lombok.Builder;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import org.locationtech.jts.geom.Coordinate;

@Builder
public record TrafficSign(
        String id,
        long roadSectionId,
        double fraction,
        String rvvCode,
        String blackCode,
        DirectionType directionType,
        String windowTime,
        String regulationOrderId,
        Coordinate location) {

}
