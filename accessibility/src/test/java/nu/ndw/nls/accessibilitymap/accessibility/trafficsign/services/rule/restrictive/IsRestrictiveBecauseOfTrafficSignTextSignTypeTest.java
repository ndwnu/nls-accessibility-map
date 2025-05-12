package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.restrictive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.springframework.stereotype.Component;

class IsRestrictiveBecauseOfTrafficSignTextSignTypeTest {

    private IsRestrictiveBecauseOfTrafficSignTextSignType isRestrictiveBecauseOfTrafficSignTextSignType;

    private TrafficSign trafficSign;

    @BeforeEach
    void setUp() {

        trafficSign = TrafficSign.builder()
                .textSigns(List.of(TextSign.builder()
                        .type(TextSignType.TIME_PERIOD)
                        .build()))
                .build();

        isRestrictiveBecauseOfTrafficSignTextSignType = new IsRestrictiveBecauseOfTrafficSignTextSignType();
    }

    @ParameterizedTest
    @EnumSource(value = TextSignType.class, mode = EnumSource.Mode.EXCLUDE, names = "TIME_PERIOD")
    void test(TextSignType textSignType) {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .trafficSignTextSignTypes(Set.of(textSignType))
                .build();

        assertThat(isRestrictiveBecauseOfTrafficSignTextSignType.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = TextSignType.class, mode = Mode.INCLUDE, names = "TIME_PERIOD")
    void test_notRelevant(TextSignType textSignType) {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .trafficSignTextSignTypes(Set.of(textSignType))
                .build();

        assertThat(isRestrictiveBecauseOfTrafficSignTextSignType.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @Test
    void test_trafficSignInvalidTextSign_null() {
        trafficSign = TrafficSign.builder()
                .textSigns(List.of(TextSign.builder()
                        .type(null)
                        .build()))
                .build();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .trafficSignTextSignTypes(Set.of(TextSignType.TIME_PERIOD))
                .build();

        assertThat(isRestrictiveBecauseOfTrafficSignTextSignType.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void test_isRelevant_accessibilityRequest_missingExcludeTextSignTypes() {

        AccessibilityRequest accessibilityRequest = mock(AccessibilityRequest.class);
        when(accessibilityRequest.trafficSignTextSignTypes()).thenReturn(null);

        assertThat(isRestrictiveBecauseOfTrafficSignTextSignType.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                isRestrictiveBecauseOfTrafficSignTextSignType.getClass(),
                Component.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void implementsTrafficSignRelevancyInterface() {

        assertThat(isRestrictiveBecauseOfTrafficSignTextSignType).isInstanceOf(TrafficSignRestriction.class);
    }
}