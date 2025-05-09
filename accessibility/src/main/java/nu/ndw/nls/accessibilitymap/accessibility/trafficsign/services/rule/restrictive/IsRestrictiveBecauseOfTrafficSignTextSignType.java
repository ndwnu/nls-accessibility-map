package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.restrictive;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import org.springframework.stereotype.Component;

@Component
public class IsRestrictiveBecauseOfTrafficSignTextSignType implements TrafficSignRestriction {

    @Override
    public boolean test(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {

        if (Objects.isNull(accessibilityRequest.trafficSignTextSignTypes())) {
            return false;
        }

        return trafficSign.textSigns().stream()
                .map(TextSign::type)
                .filter(Objects::nonNull)
                .anyMatch(accessibilityRequest.trafficSignTextSignTypes()::contains);
    }
}
