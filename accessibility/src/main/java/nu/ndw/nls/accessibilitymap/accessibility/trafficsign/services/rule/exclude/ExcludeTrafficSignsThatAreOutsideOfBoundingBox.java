package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.exclude;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import org.springframework.stereotype.Component;

@Component
public class ExcludeTrafficSignsThatAreOutsideOfBoundingBox implements TrafficSignExclusion {

    @Override
    public boolean test(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {
        if (Objects.isNull(accessibilityRequest.boundingBox())) {
            return false;
        }

        return !accessibilityRequest.boundingBox().contains(trafficSign.latitude(), trafficSign.longitude());
    }
}
