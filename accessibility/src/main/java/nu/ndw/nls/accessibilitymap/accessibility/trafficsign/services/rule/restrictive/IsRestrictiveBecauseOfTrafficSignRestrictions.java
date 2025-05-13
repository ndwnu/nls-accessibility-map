package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.restrictive;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import org.springframework.stereotype.Component;

@Component
public class IsRestrictiveBecauseOfTrafficSignRestrictions implements TrafficSignRestriction {

    @Override
    public boolean test(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {

        if (!trafficSign.restrictions().hasActiveRestrictions(accessibilityRequest)) {
            return false;
        }

        return trafficSign.restrictions().isRestrictive(accessibilityRequest);
    }
}
