package nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons;

import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;

@Builder
public class MaximumRestriction extends AccessibilityRestriction<Maximum> {


    private final Maximum value;
    private final RestrictionType restrictionType;


    @Override
    public RestrictionType getTypeOfRestriction() {
        return restrictionType;
    }

    @Override
    public Maximum getValue() {
        return value;
    }

    @Override
    public boolean isMoreRestrictiveThan(AccessibilityRestriction<Maximum> other) {
        ensureSameType(other);
        return !value.isExceeding(other.getValue().value(), false);
    }
}
