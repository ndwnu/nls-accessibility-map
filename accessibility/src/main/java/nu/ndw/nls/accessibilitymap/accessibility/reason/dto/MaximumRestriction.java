package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import java.util.Objects;
import lombok.experimental.SuperBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;

@SuperBuilder(toBuilder = true)
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
    public boolean isEqual(AccessibilityRestriction<Maximum> other) {

        ensureSameType(other);
        return Objects.equals(getValue().value(), other.getValue().value());
    }
}
