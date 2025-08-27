package nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaximumRestrictionTest {

    @Test
    void constructor() {

        MaximumRestriction maximumRestriction = MaximumRestriction.builder()
                .value(Maximum.builder().value(2d).build())
                .restrictionType(RestrictionType.VEHICLE_WEIGHT)
                .build();
        assertThat(maximumRestriction.getValue()).isEqualTo(Maximum.builder().value(2d).build());
        assertThat(maximumRestriction.getTypeOfRestriction()).isEqualTo(RestrictionType.VEHICLE_WEIGHT);
    }

    @Test
    void isEqual() {

        MaximumRestriction maximumRestriction1 = MaximumRestriction.builder()
                .value(Maximum.builder().value(2d).build())
                .restrictionType(RestrictionType.VEHICLE_WEIGHT)
                .build();

        MaximumRestriction maximumRestriction2 = MaximumRestriction.builder()
                .value(Maximum.builder().value(2d).build())
                .restrictionType(RestrictionType.VEHICLE_WEIGHT)
                .build();

        assertThat(maximumRestriction1.isEqual(maximumRestriction2)).isTrue();
    }


    @Test
    void isEqual_notEqual_invalidValue() {

        MaximumRestriction maximumRestriction1 = MaximumRestriction.builder()
                .value(Maximum.builder().value(2d).build())
                .restrictionType(RestrictionType.VEHICLE_WEIGHT)
                .build();

        MaximumRestriction maximumRestriction2 = MaximumRestriction.builder()
                .value(Maximum.builder().value(3d).build())
                .restrictionType(RestrictionType.VEHICLE_WEIGHT)
                .build();

        assertThat(maximumRestriction1.isEqual(maximumRestriction2)).isFalse();
    }

    @Test
    void isEqual_notEqual_invalidRestrictionType() {

        MaximumRestriction maximumRestriction1 = MaximumRestriction.builder()
                .value(Maximum.builder().value(2d).build())
                .restrictionType(RestrictionType.VEHICLE_WEIGHT)
                .build();

        MaximumRestriction maximumRestriction2 = MaximumRestriction.builder()
                .value(Maximum.builder().value(2d).build())
                .restrictionType(RestrictionType.VEHICLE_TYPE)
                .build();

        assertThat(catchThrowable(() -> maximumRestriction1.isEqual(maximumRestriction2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot compare accessibility restrictions of different types");
    }
}