package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.relevance;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import org.springframework.stereotype.Component;

@Component
public class IsOnlyRelevantIfZoneCodeOfTypeDetected implements TrafficSignRelevancy {

    @Override
    public boolean test(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {
        if (Objects.isNull(accessibilityRequest.excludeZoneCodeTypes())
                || Objects.isNull(trafficSign.zoneCodeType())) {
            return true; // continue
        }

        return !accessibilityRequest.excludeZoneCodeTypes().contains(trafficSign.zoneCodeType());
    }
}
