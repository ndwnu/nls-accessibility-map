package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.relevance;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import org.springframework.stereotype.Component;

@Component
public class HasRelevantRestrictions implements TrafficSignRelevancy {

    @Override
    public boolean test(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {
        if (!trafficSign.restrictions().hasActiveRestrictions(accessibilityRequest)) {
            return true; // continue
        }

        return trafficSign.restrictions().isRestrictive(accessibilityRequest);
    }
}
