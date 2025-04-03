package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.relevance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.ZoneCodeType;
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
class IsOnlyRelevantIfZoneCodeOfTypeDetectedTest {

    private IsOnlyRelevantIfZoneCodeOfTypeDetected isOnlyRelevantIfZoneCodeOfTypeDetected;

    private TrafficSign trafficSign;

    @BeforeEach
    void setUp() {

        trafficSign = TrafficSign.builder()
                .zoneCodeType(ZoneCodeType.START)
                .build();

        isOnlyRelevantIfZoneCodeOfTypeDetected = new IsOnlyRelevantIfZoneCodeOfTypeDetected();
    }

    @ParameterizedTest
    @EnumSource(value = ZoneCodeType.class, mode = Mode.EXCLUDE, names = "START")
    void test(ZoneCodeType zoneCodeType) {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeZoneCodeTypes(Set.of(zoneCodeType))
                .build();

        assertThat(isOnlyRelevantIfZoneCodeOfTypeDetected.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ZoneCodeType.class, mode = Mode.INCLUDE, names = "START")
    void test_notRelevant(ZoneCodeType zoneCodeType) {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeZoneCodeTypes(Set.of(zoneCodeType))
                .build();

        assertThat(isOnlyRelevantIfZoneCodeOfTypeDetected.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void test_isRelevant_accessibilityRequest_missingExcludeZoneCodeTypes() {

        AccessibilityRequest accessibilityRequest = mock(AccessibilityRequest.class);
        when(accessibilityRequest.excludeZoneCodeTypes()).thenReturn(null);

        assertThat(isOnlyRelevantIfZoneCodeOfTypeDetected.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @Test
    void test_isRelevant_trafficSign_missingZoneCodeType() {

        trafficSign = trafficSign.withZoneCodeType(null);

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeZoneCodeTypes(Set.of(ZoneCodeType.START))
                .build();

        assertThat(isOnlyRelevantIfZoneCodeOfTypeDetected.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @Test
    void test_isRelevant_trafficSign_missingZoneCodeType_and_accessibilityRequest_missingExcludeZoneCodeTypes() {

        trafficSign = trafficSign.withZoneCodeType(null);

        AccessibilityRequest accessibilityRequest = mock(AccessibilityRequest.class);
        when(accessibilityRequest.excludeZoneCodeTypes()).thenReturn(null);

        assertThat(isOnlyRelevantIfZoneCodeOfTypeDetected.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                isOnlyRelevantIfZoneCodeOfTypeDetected.getClass(),
                Component.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void implementsTrafficSignRelevancyInterface() {

        assertThat(isOnlyRelevantIfZoneCodeOfTypeDetected).isInstanceOf(TrafficSignRelevancy.class);
    }
}