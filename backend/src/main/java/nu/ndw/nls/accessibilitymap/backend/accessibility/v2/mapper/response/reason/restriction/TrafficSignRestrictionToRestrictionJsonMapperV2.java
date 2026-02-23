package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason.restriction;

import java.util.UUID;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.TrafficSignRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.TrafficSignTypeJson;
import org.springframework.stereotype.Component;

@Component
public class TrafficSignRestrictionToRestrictionJsonMapperV2 implements AccessibilityRestrictionJsonMapperV2<TrafficSign> {

    @Override
    public RestrictionJson map(TrafficSign trafficSign) {
        return TrafficSignRestrictionJson.builder()
                .type(TypeEnum.TRAFFIC_SIGN)
                .trafficSignId(UUID.fromString(trafficSign.externalId()))
                .trafficSignType(TrafficSignTypeJson.fromValue(trafficSign.trafficSignType().getRvvCode()))
                .build();
    }

    @Override
    public Class<? extends Restriction> getRestrictionType() {
        return TrafficSign.class;
    }
}
