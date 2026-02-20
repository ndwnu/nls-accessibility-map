package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaximumReasonTest extends ValidationTest {

    @Mock
    private ReasonType reasonType1;

    @Mock
    private ReasonType reasonType2;

    @Mock
    private Restriction restriction1;

    @Mock
    private Restriction restriction2;

    @Test
    void validate() {

        MaximumReason maximumReason = MaximumReason.builder()
                .value(Maximum.builder().value(2d).build())
                .reasonType(reasonType1)
                .build();

        validate(maximumReason, List.of(), List.of());
    }

    @Test
    void validate_value_null() {

        MaximumReason maximumReason = MaximumReason.builder()
                .value(null)
                .reasonType(reasonType1)
                .build();

        validate(maximumReason, List.of("value"), List.of("must not be null"));
    }

    @Test
    void validate_reasonType_null() {

        MaximumReason maximumReason = MaximumReason.builder()
                .value(Maximum.builder().value(2d).build())
                .reasonType(null)
                .build();

        validate(maximumReason, List.of("reasonType"), List.of("must not be null"));
    }

    @Test
    void reduce_areEqual() {

        MaximumReason maximumReason1 = MaximumReason.builder()
                .value(Maximum.builder().value(2d).build())
                .reasonType(reasonType1)
                .restrictions(new HashSet<>(Set.of(restriction1)))
                .build();
        MaximumReason maximumReason2 = MaximumReason.builder()
                .value(Maximum.builder().value(2d).build())
                .reasonType(reasonType1)
                .restrictions(new HashSet<>(Set.of(restriction2)))
                .build();

        AccessibilityReason<Maximum> newReason = maximumReason1.reduce(maximumReason2);

        assertThat(newReason).isEqualTo(maximumReason1);
        assertThat(newReason.getReasonType()).isEqualTo(reasonType1);
        assertThat(newReason.getValue()).isEqualTo(Maximum.builder().value(2d).build());
        assertThat(newReason.getRestrictions()).containsExactlyInAnyOrder(restriction1, restriction2);
    }

    @Test
    void reduce_reasonOneIsSmaller() {

        MaximumReason maximumReason1 = MaximumReason.builder()
                .value(Maximum.builder().value(1d).build())
                .reasonType(reasonType1)
                .restrictions(new HashSet<>(Set.of(restriction1)))
                .build();
        MaximumReason maximumReason2 = MaximumReason.builder()
                .value(Maximum.builder().value(2d).build())
                .reasonType(reasonType1)
                .restrictions(new HashSet<>(Set.of(restriction2)))
                .build();

        AccessibilityReason<Maximum> newReason = maximumReason1.reduce(maximumReason2);

        assertThat(newReason).isEqualTo(maximumReason1);
        assertThat(newReason.getReasonType()).isEqualTo(reasonType1);
        assertThat(newReason.getValue()).isEqualTo(Maximum.builder().value(1d).build());
        assertThat(newReason.getRestrictions()).containsExactlyInAnyOrder(restriction1);
    }

    @Test
    void reduce_reasonOneIsLarger() {

        MaximumReason maximumReason1 = MaximumReason.builder()
                .value(Maximum.builder().value(3d).build())
                .reasonType(reasonType1)
                .restrictions(new HashSet<>(Set.of(restriction1)))
                .build();
        MaximumReason maximumReason2 = MaximumReason.builder()
                .value(Maximum.builder().value(2d).build())
                .reasonType(reasonType1)
                .restrictions(new HashSet<>(Set.of(restriction2)))
                .build();

        AccessibilityReason<Maximum> newReason = maximumReason1.reduce(maximumReason2);

        assertThat(newReason).isEqualTo(maximumReason2);
        assertThat(newReason.getReasonType()).isEqualTo(reasonType1);
        assertThat(newReason.getValue()).isEqualTo(Maximum.builder().value(2d).build());
        assertThat(newReason.getRestrictions()).containsExactlyInAnyOrder(restriction2);
    }

    @Test
    void reduce_notOfSameType() {

        MaximumReason maximumReason1 = MaximumReason.builder()
                .value(Maximum.builder().value(2d).build())
                .reasonType(reasonType1)
                .restrictions(new HashSet<>(Set.of(restriction1)))
                .build();
        MaximumReason maximumReason2 = MaximumReason.builder()
                .value(Maximum.builder().value(2d).build())
                .reasonType(reasonType2)
                .restrictions(new HashSet<>(Set.of(restriction2)))
                .build();

        assertThatThrownBy(() -> maximumReason1.reduce(maximumReason2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot compare accessibility restrictions of different types");
    }

    @Override
    protected Class<?> getClassToTest() {

        return MaximumReason.class;
    }
}
