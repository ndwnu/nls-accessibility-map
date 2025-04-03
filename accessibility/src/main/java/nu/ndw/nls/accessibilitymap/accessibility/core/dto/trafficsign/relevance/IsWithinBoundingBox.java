package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.relevance;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import org.springframework.stereotype.Component;

@Component
public class IsWithinBoundingBox implements TrafficSignRelevancy {

    @Override
    public boolean test(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {
        if (Objects.isNull(accessibilityRequest.boundingBox())) {
            return true; // continue
        }

        return accessibilityRequest.boundingBox().contains(trafficSign.latitude(), trafficSign.longitude());
    }
}
