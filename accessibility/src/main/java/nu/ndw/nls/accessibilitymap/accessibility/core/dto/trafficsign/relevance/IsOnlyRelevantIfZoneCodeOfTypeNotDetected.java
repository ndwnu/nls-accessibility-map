package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.relevance;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import org.springframework.stereotype.Component;

@Component
public class IsOnlyRelevantIfZoneCodeOfTypeNotDetected implements TrafficSignRelevancy {

    @Override
    public boolean test(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {
        if (Objects.isNull(accessibilityRequest.excludeTrafficSignZoneCodeTypes())
                || Objects.isNull(trafficSign.zoneCodeType())) {
            return true; // continue
        }

        return !accessibilityRequest.excludeTrafficSignZoneCodeTypes().contains(trafficSign.zoneCodeType());
    }
}
