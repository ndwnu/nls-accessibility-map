package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.relevance;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import org.springframework.stereotype.Component;

@Component
public class HasRelevantTextSignOfType implements TrafficSignRelevancy {

    @Override
    public boolean test(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {
        if (Objects.isNull(accessibilityRequest.trafficSignTextSignTypes())) {
            return true; // continue
        }

        return trafficSign.textSigns().stream()
                .map(TextSign::type)
                .filter(Objects::nonNull)
                .anyMatch(accessibilityRequest.trafficSignTextSignTypes()::contains);
    }
}
