package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import org.springframework.validation.annotation.Validated;

@SuperBuilder
@Validated
@Getter
public class MaximumReason extends AccessibilityReason<Maximum> {

    @NotNull
    private final Maximum value;

    @NotNull
    private final ReasonType reasonType;

    @Override
    public AccessibilityReason<Maximum> reduce(AccessibilityReason<?> other) {
        AccessibilityReason<Maximum> otherMaximumReason = ensureSameType(other);

        if(Objects.equals(otherMaximumReason.getValue().value(), getValue().value())) {
            this.getRestrictions().addAll(otherMaximumReason.getRestrictions());
            return this;
        }

        return otherMaximumReason.getValue().value() < getValue().value() ? otherMaximumReason : this;
    }
}
