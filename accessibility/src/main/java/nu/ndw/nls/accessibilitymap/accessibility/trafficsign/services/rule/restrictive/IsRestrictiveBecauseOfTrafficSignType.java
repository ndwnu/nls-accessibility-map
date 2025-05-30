package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.restrictive;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import org.springframework.stereotype.Component;

@Component
public class IsRestrictiveBecauseOfTrafficSignType implements TrafficSignRestriction {

    @Override
    public boolean test(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {

        if (Objects.isNull(accessibilityRequest.trafficSignTypes())) {
            return false;
        }

        return accessibilityRequest.trafficSignTypes().contains(trafficSign.trafficSignType());
    }
}
