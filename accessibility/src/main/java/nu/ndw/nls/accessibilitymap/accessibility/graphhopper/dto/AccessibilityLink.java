package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.routingmapmatcher.network.annotations.EncodedValue;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import nu.ndw.nls.routingmapmatcher.network.model.Link;
import org.locationtech.jts.geom.LineString;

@Getter
public class AccessibilityLink extends Link {

    public static final String MUNICIPALITY_CODE = "municipality_code";

    private final DirectionalDto<Boolean> accessibility;

    @EncodedValue(key = MUNICIPALITY_CODE, bits = 17)
    private final Integer municipalityCode;

    @Builder
    protected AccessibilityLink(long id, long fromNodeId, long toNodeId,
            DirectionalDto<Boolean> accessibility,
            double distanceInMeters,
            LineString geometry,
            Integer municipalityCode) {

        super(id, fromNodeId, toNodeId, distanceInMeters, geometry);

        this.accessibility = accessibility;
        this.municipalityCode = municipalityCode;
    }
}
