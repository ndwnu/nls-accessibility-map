package nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.BICYCLE;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.BUS;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.CAR;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.CONDUCTORS;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.DELIVERY_VAN;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.MOPED;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.MOTORCYCLE;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.PEDESTRIAN;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.RIDERS;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.TAXI;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.TRACTOR;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.TRUCK;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import org.apache.commons.collections4.CollectionUtils;

@SuperBuilder(toBuilder = true)
public class TransportTypeRestriction extends AccessibilityRestriction<Set<TransportType>> {

    private static final List<TransportType> TRANSPORT_TYPES = List.of(PEDESTRIAN,
            BICYCLE,
            RIDERS,
            CONDUCTORS,
            MOPED,
            MOTORCYCLE,
            CAR,
            TAXI,
            DELIVERY_VAN,
            BUS,
            TRACTOR,
            TRUCK
    );
//    VEHICLE_WITH_TRAILER,
    //   TRAM,   CARAVAN,
//    VEHICLE_WITH_DANGEROUS_SUPPLIES

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
