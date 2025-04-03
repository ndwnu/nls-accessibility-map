package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.relevance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;

@ExtendWith(MockitoExtension.class)
class IsOnlyRelevantIfTextSignOfTypeDetectedTest {

    private IsOnlyRelevantIfTextSignOfTypeDetected isOnlyRelevantIfTextSignOfTypeDetected;

    private TrafficSign trafficSign;

    @BeforeEach
    void setUp() {

        trafficSign = TrafficSign.builder()
                .textSigns(List.of(TextSign.builder()
                        .type(TextSignType.TIME_PERIOD)
                        .build()))
                .build();

        isOnlyRelevantIfTextSignOfTypeDetected = new IsOnlyRelevantIfTextSignOfTypeDetected();
    }

    @ParameterizedTest
    @EnumSource(value = TextSignType.class, mode = EnumSource.Mode.EXCLUDE, names = "TIME_PERIOD")
    void test(TextSignType textSignType) {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeTrafficSignTextSignTypes(Set.of(textSignType))
                .build();

        assertThat(isOnlyRelevantIfTextSignOfTypeDetected.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = TextSignType.class, mode = Mode.INCLUDE, names = "TIME_PERIOD")
    void test_notRelevant(TextSignType textSignType) {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeTrafficSignTextSignTypes(Set.of(textSignType))
                .build();

        assertThat(isOnlyRelevantIfTextSignOfTypeDetected.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void test_trafficSignInvalidTextSign_null() {
        trafficSign = TrafficSign.builder()
                .textSigns(List.of(TextSign.builder()
                        .type(null)
                        .build()))
                .build();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeTrafficSignTextSignTypes(Set.of(TextSignType.TIME_PERIOD))
                .build();

        assertThat(isOnlyRelevantIfTextSignOfTypeDetected.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @Test
    void test_isRelevant_accessibilityRequest_missingExcludeTextSignTypes() {

        AccessibilityRequest accessibilityRequest = mock(AccessibilityRequest.class);
        when(accessibilityRequest.excludeTrafficSignTextSignTypes()).thenReturn(null);

        assertThat(isOnlyRelevantIfTextSignOfTypeDetected.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                isOnlyRelevantIfTextSignOfTypeDetected.getClass(),
                Component.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void implementsTrafficSignRelevancyInterface() {

        assertThat(isOnlyRelevantIfTextSignOfTypeDetected).isInstanceOf(TrafficSignRelevancy.class);
    }
}