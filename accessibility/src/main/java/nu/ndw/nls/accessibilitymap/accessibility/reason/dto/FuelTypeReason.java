package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;

@SuperBuilder
@Validated
@Getter
public class FuelTypeReason extends AccessibilityReason<Set<FuelType>> {

    @NotNull
    private final Set<FuelType> value;

    @Override
    public ReasonType getReasonType() {

        return ReasonType.FUEL_TYPE;
    }

    @Override
    public AccessibilityReason<Set<FuelType>> reduce(AccessibilityReason<?> other) {
        AccessibilityReason<Set<FuelType>> otherFuelTypeReason = ensureSameType(other);

        if (CollectionUtils.isEqualCollection(getValue(), otherFuelTypeReason.getValue())) {
            this.setRestrictions(mergeRestrictions(otherFuelTypeReason));
            return this;
        }

        if (getValue().containsAll(otherFuelTypeReason.getValue())) {
            this.setRestrictions(mergeRestrictions(otherFuelTypeReason));
            return this;
        }

        return FuelTypeReason.builder()
                .value(Stream.concat(
                                getValue().stream(),
                                otherFuelTypeReason.getValue().stream())
                        .collect(Collectors.toSet()))
                .restrictions(mergeRestrictions(otherFuelTypeReason))
                .build();
    }

    private Set<Restriction> mergeRestrictions(AccessibilityReason<Set<FuelType>> otherFuelTypeReason) {
        return Stream.concat(
                        getRestrictions().stream(),
                        otherFuelTypeReason.getRestrictions().stream())
                .collect(Collectors.toSet());
    }
}
