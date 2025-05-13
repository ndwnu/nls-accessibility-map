package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.restrictive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;

@ExtendWith(MockitoExtension.class)
class IsRestrictiveBecauseOfTrafficSignRestrictionsTest {

    private IsRestrictiveBecauseOfTrafficSignRestrictions isRestrictiveBecauseOfTrafficSignRestrictions;

    private TrafficSign trafficSign;

    @Mock
    private Restrictions restrictions;

    @Mock
    private AccessibilityRequest accessibilityRequest;

    @BeforeEach
    void setUp() {

        trafficSign = TrafficSign.builder()
                .restrictions(restrictions)
                .build();

        isRestrictiveBecauseOfTrafficSignRestrictions = new IsRestrictiveBecauseOfTrafficSignRestrictions();
    }

    @Test
    void test_isRestrictive() {

        when(restrictions.hasActiveRestrictions(accessibilityRequest)).thenReturn(true);
        when(restrictions.isRestrictive(accessibilityRequest)).thenReturn(true);

        assertThat(isRestrictiveBecauseOfTrafficSignRestrictions.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @Test
    void test_isNotRestrictive() {

        when(restrictions.hasActiveRestrictions(accessibilityRequest)).thenReturn(true);
        when(restrictions.isRestrictive(accessibilityRequest)).thenReturn(false);

        assertThat(isRestrictiveBecauseOfTrafficSignRestrictions.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void test_noActiveRestrictions() {

        when(restrictions.hasActiveRestrictions(accessibilityRequest)).thenReturn(false);

        assertThat(isRestrictiveBecauseOfTrafficSignRestrictions.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                isRestrictiveBecauseOfTrafficSignRestrictions.getClass(),
                Component.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void implementsTrafficSignRelevancyInterface() {

        assertThat(isRestrictiveBecauseOfTrafficSignRestrictions).isInstanceOf(TrafficSignRestriction.class);
    }
}