package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityReasonGroupTest {

    @Mock
    private AccessibilityReason<?> accessibilityReason;

    @Test
    void constructor() {

        AccessibilityReasonGroup accessibilityReasonGroup = new AccessibilityReasonGroup(List.of(accessibilityReason));

        assertThat(accessibilityReasonGroup).containsExactly(accessibilityReason);
    }
}
