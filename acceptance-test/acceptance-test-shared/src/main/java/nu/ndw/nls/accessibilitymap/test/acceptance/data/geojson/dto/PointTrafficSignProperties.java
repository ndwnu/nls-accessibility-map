package nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointTrafficSignProperties implements Properties {

    private final UUID trafficSignId;

    private final long roadSectionId;

    private final Double fraction;

    private final String rvvCode;

    private final String drivingDirection;
}
