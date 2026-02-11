package nu.ndw.nls.accessibilitymap.job.mapgenerator.export.geojson.dto;

import java.net.URI;
import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;

@Getter
@Builder
public class TrafficSignProperties implements Properties {

    private long nwbRoadSectionId;

    private String trafficSignId;

    private boolean accessible;

    private Direction direction;

    private TrafficSignType trafficSignType;

    private String windowTimes;

    private URI iconUrl;

    private boolean isTrafficSign;
}
