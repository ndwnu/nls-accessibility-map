package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import org.springframework.validation.annotation.Validated;

@SuperBuilder
@Validated
@Getter
public class TransportTypeReason extends AccessibilityReason<Set<TransportType>> {

    @NotNull
    private final Set<TransportType> value;

    @Override
    public ReasonType getReasonType() {
        return ReasonType.VEHICLE_TYPE;
    }

    @Override
    public AccessibilityReason<Set<TransportType>> reduce(AccessibilityReason<?> other) {
        AccessibilityReason<Set<TransportType>> otherTransportTypeReason = ensureSameType(other);

        return TransportTypeReason.builder()
                .value(Stream.concat(
                                getValue().stream(),
                                otherTransportTypeReason.getValue().stream())
                        .collect(Collectors.toSet()))
                .restrictions(mergeRestrictions(otherTransportTypeReason))
                .build();
    }

}
