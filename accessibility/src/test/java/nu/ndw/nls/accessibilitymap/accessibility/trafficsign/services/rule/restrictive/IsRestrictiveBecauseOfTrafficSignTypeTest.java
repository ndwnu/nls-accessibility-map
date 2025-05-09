package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.restrictive;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

class IsRestrictiveBecauseOfTrafficSignTypeTest {

    private IsRestrictiveBecauseOfTrafficSignType isRestrictiveBecauseOfTrafficSignType;

    private TrafficSign trafficSign;

    @BeforeEach
    void setUp() {

        trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C6)
                .build();

        isRestrictiveBecauseOfTrafficSignType = new IsRestrictiveBecauseOfTrafficSignType();
    }

    @Test
    void test() {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .trafficSignTypes(Set.of(TrafficSignType.C6))
                .build();

        assertThat(isRestrictiveBecauseOfTrafficSignType.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @Test
    void test_notRelevant_missingTrafficSignType() {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .trafficSignTypes(Set.of(TrafficSignType.C7))
                .build();

        assertThat(isRestrictiveBecauseOfTrafficSignType.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void test_isRelevant_accessibilityRequest_missingTrafficSignTypes() {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder().build();

        assertThat(isRestrictiveBecauseOfTrafficSignType.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                isRestrictiveBecauseOfTrafficSignType.getClass(),
                Component.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void implementsTrafficSignRelevancyInterface() {

        assertThat(isRestrictiveBecauseOfTrafficSignType).isInstanceOf(TrafficSignRestriction.class);
    }
}