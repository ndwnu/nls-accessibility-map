package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibleReasonTest extends ValidationTest {

    @Mock
    private Restriction restriction1;

    @Mock
    private Restriction restriction2;

    @Test
    void validate() {

        AccessibleReason accessibilityReason = AccessibleReason.builder()
                .value(true)
                .build();

        validate(accessibilityReason, List.of(), List.of());
    }

    @Test
    void validate_value_null() {

        AccessibleReason accessibilityReason = AccessibleReason.builder()
                .value(null)
                .build();

        validate(accessibilityReason, List.of("value"), List.of("must not be null"));
    }

    @Test
    void defaultReasonType() {

        AccessibleReason accessibilityReason = AccessibleReason.builder().build();

        assertThat(accessibilityReason.getReasonType()).isEqualTo(ReasonType.ACCESSIBLE_REASON);
    }

    @ParameterizedTest
    @CsvSource({
            "true",
            "false"
    })
    void reduce_areEqual(Boolean value) {

        AccessibleReason accessibilityReason1 = AccessibleReason.builder()
                .value(value)
                .restrictions(new HashSet<>(Set.of(restriction1)))
                .build();
        AccessibleReason accessibilityReason2 = AccessibleReason.builder()
                .value(value)
                .restrictions(new HashSet<>(Set.of(restriction2)))
                .build();

        AccessibilityReason<Boolean> newReason = accessibilityReason1.reduce(accessibilityReason2);

        assertThat(newReason)
                .isNotNull()
                .isInstanceOf(AccessibleReason.class)
                .isNotEqualTo(accessibilityReason1)
                .isNotEqualTo(accessibilityReason2);

        assertThat(newReason.getValue()).isEqualTo(value);
        if(value) {
            assertThat(newReason.getRestrictions()).containsExactly(restriction2);
        } else {
            assertThat(newReason.getRestrictions()).containsExactlyInAnyOrder(restriction1, restriction2);
        }
    }

    @Test
    void reduce_reasonOneIsFalse_reasonTwoIsTrue_reasonOneShouldPrevail() {

        AccessibleReason accessibilityReason1 = AccessibleReason.builder()
                .value(false)
                .restrictions(new HashSet<>(Set.of(restriction1)))
                .build();
        AccessibleReason accessibilityReason2 = AccessibleReason.builder()
                .value(true)
                .restrictions(new HashSet<>(Set.of(restriction2)))
                .build();

        AccessibilityReason<Boolean> newReason = accessibilityReason1.reduce(accessibilityReason2);

        assertThat(newReason)
                .isNotNull()
                .isInstanceOf(AccessibleReason.class)
                .isNotEqualTo(accessibilityReason1)
                .isNotEqualTo(accessibilityReason2);

        assertThat(newReason.getValue()).isFalse();
        assertThat(newReason.getRestrictions()).containsExactlyInAnyOrder(restriction1);
    }

    @Test
    void reduce_reasonOneIsTrue_reasonTwoIsFalse_reasonTwoShouldPrevail() {

        AccessibleReason accessibilityReason1 = AccessibleReason.builder()
                .value(true)
                .restrictions(new HashSet<>(Set.of(restriction1)))
                .build();
        AccessibleReason accessibilityReason2 = AccessibleReason.builder()
                .value(false)
                .restrictions(new HashSet<>(Set.of(restriction2)))
                .build();

        AccessibilityReason<Boolean> newReason = accessibilityReason1.reduce(accessibilityReason2);

        assertThat(newReason)
                .isNotNull()
                .isInstanceOf(AccessibleReason.class)
                .isNotEqualTo(accessibilityReason1)
                .isNotEqualTo(accessibilityReason2);

        assertThat(newReason.getValue()).isFalse();
        assertThat(newReason.getRestrictions()).containsExactlyInAnyOrder(restriction2);
    }


    @Test
    void reduce_notOfSameType() {

        AccessibleReason accessibilityReason1 = AccessibleReason.builder().build();
        AccessibilityReason<?> otherReason = mock(AccessibilityReason.class);
        when(otherReason.getReasonType()).thenReturn(mock(ReasonType.class));

        assertThatThrownBy(() -> accessibilityReason1.reduce(otherReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot compare accessibility restrictions of different types");
    }


    @Override
    protected Class<?> getClassToTest() {
        return AccessibleReason.class;
    }
}
