package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.exclude;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.stereotype.Component;

class ExcludeTrafficSignsWithTextSignTypesTest {

    private ExcludeTrafficSignsWithTextSignTypes excludeTrafficSignsWithTextSignTypes;

    @BeforeEach
    void setUp() {

        excludeTrafficSignsWithTextSignTypes = new ExcludeTrafficSignsWithTextSignTypes();
    }

    @ParameterizedTest
    @EnumSource(value = TextSignType.class)
    void test(TextSignType textSignType) {

        TrafficSign trafficSign = TrafficSign.builder()
                .textSigns(List.of(TextSign.builder()
                        .type(textSignType)
                        .build()))
                .build();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeTrafficSignTextSignTypes(Set.of(textSignType))
                .build();

        assertThat(excludeTrafficSignsWithTextSignTypes.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = TextSignType.class)
    void test_notRelevant(TextSignType textSignType) {

        TrafficSign trafficSign = TrafficSign.builder()
                .textSigns(List.of(TextSign.builder()
                        .type(textSignType)
                        .build()))
                .build();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeTrafficSignTextSignTypes(Arrays.stream(TextSignType.values())
                        .filter(type -> type != textSignType)
                        .collect(Collectors.toSet()))
                .build();

        assertThat(excludeTrafficSignsWithTextSignTypes.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void test_trafficSignInvalidTextSign_null() {

        TrafficSign trafficSign = TrafficSign.builder()
                .textSigns(List.of(TextSign.builder()
                        .type(null)
                        .build()))
                .build();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeTrafficSignTextSignTypes(Set.of(TextSignType.TIME_PERIOD))
                .build();

        assertThat(excludeTrafficSignsWithTextSignTypes.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void test_isRelevant_accessibilityRequest_missingExcludeTextSignTypes() {

        TrafficSign trafficSign = TrafficSign.builder()
                .textSigns(List.of(TextSign.builder()
                        .type(TextSignType.FREE_TEXT)
                        .build()))
                .build();

        AccessibilityRequest accessibilityRequest = mock(AccessibilityRequest.class);
        when(accessibilityRequest.excludeTrafficSignTextSignTypes()).thenReturn(null);

        assertThat(excludeTrafficSignsWithTextSignTypes.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                excludeTrafficSignsWithTextSignTypes.getClass(),
                Component.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void implementsTrafficSignRelevancyInterface() {

        assertThat(excludeTrafficSignsWithTextSignTypes).isInstanceOf(TrafficSignExclusion.class);
    }
}