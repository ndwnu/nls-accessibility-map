package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
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

        if(this.getValue().equals(otherAccessibleReason.getValue())) {
            this.getRestrictions().addAll(otherAccessibleReason.getRestrictions());
            return this;
        }

        if (this.getValue().equals(false)) {
            return this;
        } else {
            return otherAccessibleReason;
        }
    }
}
