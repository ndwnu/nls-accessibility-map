package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransportTypeReasonTest extends ValidationTest {

    @Mock
    private Restriction restriction1;

    @Mock
    private Restriction restriction2;

    @Mock
    private TransportType transportType1;

    @Mock
    private TransportType transportType2;

    @Test
    void validate() {

        TransportTypeReason transportTypeReason = TransportTypeReason.builder()
                .value(Set.of(transportType1))
                .build();

        validate(transportTypeReason, List.of(), List.of());
    }

    @Test
    void validate_value_null() {

        TransportTypeReason transportTypeReason = TransportTypeReason.builder()
                .value(null)
                .build();

        validate(transportTypeReason, List.of("value"), List.of("must not be null"));
    }

    @Test
    void defaultReasonType() {

        TransportTypeReason transportTypeReason = TransportTypeReason.builder().build();

        assertThat(transportTypeReason.getReasonType()).isEqualTo(ReasonType.VEHICLE_TYPE);
    }

    @Test
    void reduce_areEqual() {

        TransportTypeReason transportTypeReason1 = TransportTypeReason.builder()
                .value(Set.of(transportType1))
                .restrictions(new HashSet<>(Set.of(restriction1)))
                .build();
        TransportTypeReason transportTypeReason2 = TransportTypeReason.builder()
                .value(Set.of(transportType1))
                .restrictions(new HashSet<>(Set.of(restriction2)))
                .build();

        AccessibilityReason<Set<TransportType>> newReason = transportTypeReason1.reduce(transportTypeReason2);

        assertThat(newReason).isEqualTo(transportTypeReason1);
        assertThat(newReason.getValue()).containsExactly(transportType1);
        assertThat(newReason.getRestrictions()).containsExactlyInAnyOrder(restriction1, restriction2);
    }

    @Test
    void reduce_reasonOneContainsAllReasonsOfOther() {

        TransportTypeReason transportTypeReason1 = TransportTypeReason.builder()
                .value(Set.of(transportType1, transportType2))
                .restrictions(new HashSet<>(Set.of(restriction1)))
                .build();
        TransportTypeReason transportTypeReason2 = TransportTypeReason.builder()
                .value(Set.of(transportType1))
                .restrictions(new HashSet<>(Set.of(restriction2)))
                .build();

        AccessibilityReason<Set<TransportType>> newReason = transportTypeReason1.reduce(transportTypeReason2);

        assertThat(newReason).isEqualTo(transportTypeReason1);
        assertThat(newReason.getValue()).containsExactlyInAnyOrder(transportType1, transportType2);
        assertThat(newReason.getRestrictions()).containsExactlyInAnyOrder(restriction1, restriction2);
    }

    @Test
    void reduce_reasonOneDoesNotContainAllReasonsOfOther() {

        TransportTypeReason transportTypeReason1 = TransportTypeReason.builder()
                .value(Set.of(transportType1))
                .restrictions(new HashSet<>(Set.of(restriction1)))
                .build();
        TransportTypeReason transportTypeReason2 = TransportTypeReason.builder()
                .value(Set.of(transportType2))
                .restrictions(new HashSet<>(Set.of(restriction2)))
                .build();

        AccessibilityReason<Set<TransportType>> newReason = transportTypeReason1.reduce(transportTypeReason2);

        assertThat(newReason)
                .isNotNull()
                .isInstanceOf(TransportTypeReason.class)
                .isNotEqualTo(transportTypeReason1)
                .isNotEqualTo(transportTypeReason2);
        assertThat(newReason.getValue()).containsExactlyInAnyOrder(transportType1, transportType2);
        assertThat(newReason.getRestrictions()).containsExactlyInAnyOrder(restriction1, restriction2);
    }
    @Test
    void reduce_notOfSameType() {

        TransportTypeReason transportTypeReason1 = TransportTypeReason.builder().build();
        AccessibilityReason<?> otherReason = mock(AccessibilityReason.class);
        when(otherReason.getReasonType()).thenReturn(mock(ReasonType.class));

        assertThatThrownBy(() -> transportTypeReason1.reduce(otherReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot compare accessibility restrictions of different types");
    }
    @Override
    protected Class<?> getClassToTest() {

        return TransportTypeReason.class;
    }
}
