package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransportRestrictionsTest {

    @Mock
    private TransportConditions restrictions;

    @Mock
    private TransportConditions exceptionsA;

    @Mock
    private TransportConditions exceptionsB;

    private TransportRestrictions transportRestrictions;

    @Mock
    private AccessibilityRequest accessibilityRequest;

    @BeforeEach
    void setUp() {
        transportRestrictions = TransportRestrictions.builder()
                .restrictions(restrictions)
                .exemptions(List.of(exceptionsA, exceptionsB))
                .build();
    }

    @Test
    void isRestrictive_restrictionConditionsDoNotApply() {
        when(restrictions.conditionsApply(accessibilityRequest)).thenReturn(false);
        assertThat(transportRestrictions.isRestrictive(accessibilityRequest)).isFalse();
        verifyNoInteractions(exceptionsA, exceptionsB);
    }

    @Test
    void isRestrictive_restrictionConditionApplyNoExceptions() {
        when(restrictions.conditionsApply(accessibilityRequest)).thenReturn(true);
        when(exceptionsA.conditionsApply(accessibilityRequest)).thenReturn(false);
        when(exceptionsA.conditionsApply(accessibilityRequest)).thenReturn(false);

        assertThat(transportRestrictions.isRestrictive(accessibilityRequest)).isTrue();
    }

    @Test
    void isRestrictive_restrictionConditionAndExemptionAApplies() {
        when(restrictions.conditionsApply(accessibilityRequest)).thenReturn(true);
        when(exceptionsA.conditionsApply(accessibilityRequest)).thenReturn(true);

        assertThat(transportRestrictions.isRestrictive(accessibilityRequest)).isFalse();
    }

    @Test
    void isRestrictive_restrictionConditionAndExemptionBApplies() {
        when(restrictions.conditionsApply(accessibilityRequest)).thenReturn(true);
        when(exceptionsA.conditionsApply(accessibilityRequest)).thenReturn(false);
        when(exceptionsB.conditionsApply(accessibilityRequest)).thenReturn(true);

        assertThat(transportRestrictions.isRestrictive(accessibilityRequest)).isFalse();
    }
}
