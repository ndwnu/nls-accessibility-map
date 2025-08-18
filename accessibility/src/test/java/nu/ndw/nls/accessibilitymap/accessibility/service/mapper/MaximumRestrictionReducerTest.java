package nu.ndw.nls.accessibilitymap.accessibility.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MaximumRestrictionReducerTest {

    private MaximumRestrictionReducer maximumRestrictionReducer;

    @BeforeEach
    void setUp() {
        maximumRestrictionReducer = new MaximumRestrictionReducer();
    }

    @Test
    void reduceRestrictions_minimal() {

        MaximumRestriction maximumRestriction1 = createMaximumRestriction(2D, RestrictionType.VEHICLE_WEIGHT);
        MaximumRestriction maximumRestriction2 = createMaximumRestriction(3D, RestrictionType.VEHICLE_WEIGHT);
        // This will not be in the result
        MaximumRestriction maximumRestriction3 = createMaximumRestriction(3D, RestrictionType.VEHICLE_AXLE_LOAD);
        AccessibilityReason accessibilityReason1 = createAccessibilityReason(List.of(maximumRestriction1, maximumRestriction3));
        AccessibilityReason accessibilityReason2 = createAccessibilityReason(List.of(maximumRestriction2));
        maximumRestriction1.setAccessibilityReason(accessibilityReason1);
        maximumRestriction2.setAccessibilityReason(accessibilityReason2);
        maximumRestriction3.setAccessibilityReason(accessibilityReason1);
        List<AccessibilityReason> result = maximumRestrictionReducer.reduceRestrictions(
                List.of(maximumRestriction1, maximumRestriction2));
        assertThat(result).satisfiesExactly(item1 -> assertThat(item1.restrictions()).containsExactlyInAnyOrder(maximumRestriction1));
    }

    @Test
    void reduceRestrictions_notOfSameType_throwsException() {

        MaximumRestriction maximumRestriction1 = createMaximumRestriction(2D, RestrictionType.VEHICLE_WEIGHT);
        MaximumRestriction maximumRestriction2 = createMaximumRestriction(3D, RestrictionType.VEHICLE_AXLE_LOAD);

        AccessibilityReason accessibilityReason1 = createAccessibilityReason(List.of(maximumRestriction1));
        AccessibilityReason accessibilityReason2 = createAccessibilityReason(List.of(maximumRestriction2));
        maximumRestriction1.setAccessibilityReason(accessibilityReason1);
        maximumRestriction2.setAccessibilityReason(accessibilityReason2);

       assertThatThrownBy( ()->maximumRestrictionReducer.reduceRestrictions(
               List.of(maximumRestriction1, maximumRestriction2)))
               .withFailMessage("Cannot reduce restrictions of different types")
               .isInstanceOf(IllegalArgumentException.class);
    }

    public AccessibilityReason createAccessibilityReason(List<AccessibilityRestriction> restrictions) {
        return AccessibilityReason.builder()
                .restrictions(restrictions)
                .build();
    }

    public MaximumRestriction createMaximumRestriction(double value, RestrictionType restrictionType) {
        return MaximumRestriction.builder()
                .restrictionType(restrictionType)
                .value(Maximum.builder()
                        .value(value)
                        .build())
                .build();
    }

    @Test
    void getType() {
        assertThat(maximumRestrictionReducer.getType())
                .isEqualTo(MaximumRestriction.class);
    }
}
