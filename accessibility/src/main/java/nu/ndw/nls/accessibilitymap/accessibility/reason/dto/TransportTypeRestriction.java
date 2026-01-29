package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import java.util.Set;
import lombok.experimental.SuperBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import org.apache.commons.collections4.CollectionUtils;

@SuperBuilder(toBuilder = true)
public class TransportTypeRestriction extends AccessibilityRestriction<Set<TransportType>> {

    private final Set<TransportType> value;

    @Override
    public RestrictionType getTypeOfRestriction() {
        return RestrictionType.VEHICLE_TYPE;
    }

    @Override
    public Set<TransportType> getValue() {
        return value;
    }

    @Override
    public boolean isEqual(AccessibilityRestriction<Set<TransportType>> other) {
        ensureSameType(other);
        return CollectionUtils.isEqualCollection(getValue(), other.getValue());
    }
}
