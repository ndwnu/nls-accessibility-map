package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import org.springframework.validation.annotation.Validated;

@SuperBuilder
@Validated
@Getter
public class AccessibleReason extends AccessibilityReason<Boolean> {

    @NotNull
    private final Boolean value;

    @Override
    public ReasonType getReasonType() {

        return ReasonType.ACCESSIBLE_REASON;
    }

    @Override
    public AccessibilityReason<Boolean> reduce(AccessibilityReason<?> other) {
        AccessibilityReason<Boolean> otherAccessibleReason = ensureSameType(other);

        Boolean newValue = this.getValue();
        Set<Restriction> newRestrictions = this.getRestrictions();
        if(Boolean.TRUE.equals(value)) {
            newValue = otherAccessibleReason.getValue();
            newRestrictions = otherAccessibleReason.getRestrictions();
        } else if(Boolean.FALSE.equals(otherAccessibleReason.getValue())) {
            newRestrictions = mergeRestrictions(otherAccessibleReason);
        }

        return AccessibleReason.builder()
                .value(newValue)
                .restrictions(newRestrictions)
                .build();
    }
}
