package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import java.util.List;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;

@Builder
public record TransportRestrictions(TransportConditions restrictions, List<TransportConditions> exemptions) {

    public boolean isRestrictive(AccessibilityRequest accessibilityRequest) {

        if (!restrictions.conditionsApply(accessibilityRequest)) {
            return false;
        }

        return exemptions.stream().noneMatch(exemption -> exemption.conditionsApply(accessibilityRequest));
    }
}
