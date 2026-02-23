package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FuelTypeReasonTest extends ValidationTest {

    @Mock
    private Restriction restriction1;

    @Mock
    private Restriction restriction2;

    @Mock
    private FuelType fuelType1;

    @Mock
    private FuelType fuelType2;

    @Test
    void validate() {

        FuelTypeReason fuelTypeReason = FuelTypeReason.builder()
                .value(Set.of(fuelType1))
                .build();

        validate(fuelTypeReason, List.of(), List.of());
    }

    @Test
    void validate_value_null() {

        FuelTypeReason fuelTypeReason = FuelTypeReason.builder()
                .value(null)
                .build();

        validate(fuelTypeReason, List.of("value"), List.of("must not be null"));
    }

    @Test
    void defaultReasonType() {

        FuelTypeReason fuelTypeReason = FuelTypeReason.builder().build();

        assertThat(fuelTypeReason.getReasonType()).isEqualTo(ReasonType.FUEL_TYPE);
    }

    @Test
    void reduce_areEqual() {

        FuelTypeReason fuelTypeReason1 = FuelTypeReason.builder()
                .value(Set.of(fuelType1))
                .restrictions(new HashSet<>(Set.of(restriction1)))
                .build();
        FuelTypeReason fuelTypeReason2 = FuelTypeReason.builder()
                .value(Set.of(fuelType1))
                .restrictions(new HashSet<>(Set.of(restriction2)))
                .build();

        AccessibilityReason<Set<FuelType>> newReason = fuelTypeReason1.reduce(fuelTypeReason2);

        assertThat(newReason)
                .isNotNull()
                .isInstanceOf(FuelTypeReason.class)
                .isNotEqualTo(fuelTypeReason1)
                .isNotEqualTo(fuelTypeReason2);

        assertThat(newReason.getValue()).containsExactly(fuelType1);
        assertThat(newReason.getRestrictions()).containsExactlyInAnyOrder(restriction1, restriction2);
    }

    @Test
    void reduce_reasonOneContainsAllReasonsOfOther() {

        FuelTypeReason fuelTypeReason1 = FuelTypeReason.builder()
                .value(Set.of(fuelType1, fuelType2))
                .restrictions(new HashSet<>(Set.of(restriction1)))
                .build();
        FuelTypeReason fuelTypeReason2 = FuelTypeReason.builder()
                .value(Set.of(fuelType1))
                .restrictions(new HashSet<>(Set.of(restriction2)))
                .build();

        AccessibilityReason<Set<FuelType>> newReason = fuelTypeReason1.reduce(fuelTypeReason2);

        assertThat(newReason)
                .isNotNull()
                .isInstanceOf(FuelTypeReason.class)
                .isNotEqualTo(fuelTypeReason1)
                .isNotEqualTo(fuelTypeReason2);

        assertThat(newReason.getValue()).containsExactlyInAnyOrder(fuelType1, fuelType2);
        assertThat(newReason.getRestrictions()).containsExactlyInAnyOrder(restriction1, restriction2);
    }

    @Test
    void reduce_reasonOneDoesNotContainAllReasonsOfOther() {

        FuelTypeReason fuelTypeReason1 = FuelTypeReason.builder()
                .value(Set.of(fuelType1))
                .restrictions(new HashSet<>(Set.of(restriction1)))
                .build();
        FuelTypeReason fuelTypeReason2 = FuelTypeReason.builder()
                .value(Set.of(fuelType2))
                .restrictions(new HashSet<>(Set.of(restriction2)))
                .build();

        AccessibilityReason<Set<FuelType>> newReason = fuelTypeReason1.reduce(fuelTypeReason2);

        assertThat(newReason)
                .isNotNull()
                .isInstanceOf(FuelTypeReason.class)
                .isNotEqualTo(fuelTypeReason1)
                .isNotEqualTo(fuelTypeReason2);
        assertThat(newReason.getValue()).containsExactlyInAnyOrder(fuelType1, fuelType2);
        assertThat(newReason.getRestrictions()).containsExactlyInAnyOrder(restriction1, restriction2);
    }
    @Test
    void reduce_notOfSameType() {

        FuelTypeReason fuelTypeReason1 = FuelTypeReason.builder().build();
        AccessibilityReason<?> otherReason = mock(AccessibilityReason.class);
        when(otherReason.getReasonType()).thenReturn(mock(ReasonType.class));

        assertThatThrownBy(() -> fuelTypeReason1.reduce(otherReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot compare accessibility restrictions of different types");
    }

    @Override
    protected Class<?> getClassToTest() {

        return FuelTypeReason.class;
    }
}
