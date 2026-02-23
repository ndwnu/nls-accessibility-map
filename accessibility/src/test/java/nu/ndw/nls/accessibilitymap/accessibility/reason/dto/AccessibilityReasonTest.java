package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityReasonTest {

    @Mock
    private ReasonType reasonType1;

    @Mock
    private ReasonType reasonType2;

    @Test
    void ensureSameType() {

        TestAccessibilityReason accessibilityReason1 = new TestAccessibilityReason(reasonType1);
        TestAccessibilityReason accessibilityReason2 = new TestAccessibilityReason(reasonType1);

        AccessibilityReason<Integer> actualAccessibilityReason = accessibilityReason1.ensureSameType(accessibilityReason2);

        assertThat(actualAccessibilityReason).isEqualTo(accessibilityReason2);
    }

    @Test
    void ensureSameType_notTheSameType() {

        TestAccessibilityReason accessibilityReason1 = new TestAccessibilityReason(reasonType1);
        TestAccessibilityReason accessibilityReason2 = new TestAccessibilityReason(reasonType2);

        assertThatThrownBy(() -> accessibilityReason1.ensureSameType(accessibilityReason2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot compare accessibility restrictions of different types");
    }

    private class TestAccessibilityReason extends AccessibilityReason<Integer> {

        private final ReasonType reasonType;

        public TestAccessibilityReason(ReasonType reasonType) {
            this.reasonType = reasonType;
        }

        @Override
        public ReasonType getReasonType() {
            return reasonType;
        }

        @Override
        public Integer getValue() {
            return null;
        }

        @Override
        public AccessibilityReason<Integer> reduce(AccessibilityReason<?> other) {
            return null;
        }
    }
}
