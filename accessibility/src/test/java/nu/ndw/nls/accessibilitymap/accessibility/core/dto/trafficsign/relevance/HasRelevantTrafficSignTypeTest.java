package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.relevance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;

@ExtendWith(MockitoExtension.class)
class HasRelevantTrafficSignTypeTest {

    private HasRelevantTrafficSignType hasRelevantTrafficSignType;

    private TrafficSign trafficSign;

    @BeforeEach
    void setUp() {

        trafficSign = TrafficSign.builder()
                .trafficSignType(TrafficSignType.C6)
                .build();

        hasRelevantTrafficSignType = new HasRelevantTrafficSignType();
    }

    @Test
    void test() {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .trafficSignTypes(Set.of(TrafficSignType.C6))
                .build();

        assertThat(hasRelevantTrafficSignType.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @Test
    void test_notRelevant_missingTrafficSignType() {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .trafficSignTypes(Set.of(TrafficSignType.C7))
                .build();

        assertThat(hasRelevantTrafficSignType.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void test_isRelevant_accessibilityRequest_missingTrafficSignTypes() {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder().build();

        assertThat(hasRelevantTrafficSignType.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                hasRelevantTrafficSignType.getClass(),
                Component.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void implementsTrafficSignRelevancyInterface() {

        assertThat(hasRelevantTrafficSignType).isInstanceOf(TrafficSignRelevancy.class);
    }
}