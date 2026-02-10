package nu.ndw.nls.accessibilitymap.accessibility.service.debug;

import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.Properties;

@Getter
@Builder
public class RestrictionProperties implements Properties {

    private final String type;

    private final long roadSectionId;

    private final Direction direction;

    private final double fraction;

    private final Integer trafficSignId;

    private final String trafficSignExternalId;

    private final TrafficSignType trafficSignType;

    private final Double trafficSignBlackCode;
}
