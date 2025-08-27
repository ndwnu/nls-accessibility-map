package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.exclude;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import org.springframework.stereotype.Component;

@Component
public class ExcludeTrafficSignsWithRestrictionsOfEmissionZoneType implements TrafficSignExclusion {

    @Override
    public boolean test(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {

        if (Objects.isNull(accessibilityRequest.excludeRestrictionsWithEmissionZoneTypes())
                || Objects.isNull(trafficSign.restrictions().emissionZone())) {
            return false;
        }

        return accessibilityRequest.excludeRestrictionsWithEmissionZoneTypes().contains(trafficSign.restrictions().emissionZone().type());
    }
}
