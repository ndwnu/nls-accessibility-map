package nu.ndw.nls.accessibilitymap.accessibility.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.TransportTypeRestriction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransportTypeRestrictionReducerTest {

    private TransportTypeRestrictionReducer transportTypeRestrictionReducer;

    @BeforeEach
    void setUp() {
        transportTypeRestrictionReducer = new TransportTypeRestrictionReducer();
    }

    @Test
    void reduceRestrictions_unique() {
        // this restriction will not be in the result
        MaximumRestriction maximumRestriction = MaximumRestriction.builder()
                .restrictionType(RestrictionType.VEHICLE_WIDTH)
                .build();
        TransportTypeRestriction transportTypeRestriction1 = createTransportTypeRestriction(Set.of(TransportType.CAR));
        TransportTypeRestriction transportTypeRestriction2 = createTransportTypeRestriction(Set.of(TransportType.BICYCLE));
        AccessibilityReason accessibilityReason1 = createAccessibilityReason(List.of(maximumRestriction, transportTypeRestriction1));
        AccessibilityReason accessibilityReason2 = createAccessibilityReason(List.of(transportTypeRestriction2));
        maximumRestriction.setAccessibilityReason(accessibilityReason1);
        transportTypeRestriction1.setAccessibilityReason(accessibilityReason1);
        transportTypeRestriction2.setAccessibilityReason(accessibilityReason2);
        List<AccessibilityReason> result = transportTypeRestrictionReducer.reduceRestrictions(
                List.of(transportTypeRestriction1, transportTypeRestriction2));
        assertThat(result)
                .satisfiesExactly(item1 -> assertThat(item1.restrictions()).containsExactlyInAnyOrder(transportTypeRestriction1),
                        item2 -> assertThat(item2.restrictions()).containsExactlyInAnyOrder(transportTypeRestriction2));
    }

    @Test
    void reduceRestrictions_notUnique() {
        TransportTypeRestriction transportTypeRestriction1 = createTransportTypeRestriction(Set.of(TransportType.CAR));
        TransportTypeRestriction transportTypeRestriction2 = createTransportTypeRestriction(Set.of(TransportType.CAR));
        AccessibilityReason accessibilityReason1 = createAccessibilityReason(List.of(transportTypeRestriction1));
        AccessibilityReason accessibilityReason2 = createAccessibilityReason(List.of(transportTypeRestriction2));
        transportTypeRestriction1.setAccessibilityReason(accessibilityReason1);
        transportTypeRestriction2.setAccessibilityReason(accessibilityReason2);
        List<AccessibilityReason> result = transportTypeRestrictionReducer.reduceRestrictions(
                List.of(transportTypeRestriction1, transportTypeRestriction2));
        assertThat(result)
                .satisfiesExactly(item1 -> assertThat(item1.restrictions()).containsExactlyInAnyOrder(transportTypeRestriction1));
    }

    public TransportTypeRestriction createTransportTypeRestriction(Set<TransportType> transportTypes) {
        return TransportTypeRestriction.builder()
                .value(transportTypes)
                .build();

    }

    public AccessibilityReason createAccessibilityReason(List<AccessibilityRestriction> restrictions) {
        return AccessibilityReason.builder()
                .restrictions(restrictions)
                .build();
    }

    @Test
    void getType() {
        assertThat(transportTypeRestrictionReducer.getType())
                .isEqualTo(TransportTypeRestriction.class);
    }
}
