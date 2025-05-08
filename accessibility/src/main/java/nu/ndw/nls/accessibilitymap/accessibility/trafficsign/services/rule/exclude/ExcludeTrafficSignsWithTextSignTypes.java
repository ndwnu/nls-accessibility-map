package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.exclude;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import org.springframework.stereotype.Component;

@Component
public class ExcludeTrafficSignsWithTextSignTypes implements TrafficSignExclusion {

    @Override
    public boolean test(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {

        if (Objects.isNull(accessibilityRequest.excludeTrafficSignTextSignTypes())) {
            return false;
        }

        return trafficSign.textSigns().stream()
                .map(TextSign::type)
                .filter(Objects::nonNull)
                .anyMatch(accessibilityRequest.excludeTrafficSignTextSignTypes()::contains);
    }
}
