package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.exclude;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.ZoneCodeType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.stereotype.Component;

class ExcludeTrafficSignsWithZoneCodeTypesTest {

    private ExcludeTrafficSignsWithZoneCodeTypes excludeTrafficSignsWithZoneCodeTypes;

    @BeforeEach
    void setUp() {

        excludeTrafficSignsWithZoneCodeTypes = new ExcludeTrafficSignsWithZoneCodeTypes();
    }

    @ParameterizedTest
    @EnumSource(value = ZoneCodeType.class)
    void test_isExcluded(ZoneCodeType zoneCodeType) {

        TrafficSign trafficSign = TrafficSign.builder()
                .zoneCodeType(zoneCodeType)
                .build();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeTrafficSignZoneCodeTypes(Set.of(zoneCodeType))
                .build();

        assertThat(excludeTrafficSignsWithZoneCodeTypes.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ZoneCodeType.class)
    void test_isNotExcluded(ZoneCodeType zoneCodeType) {

        TrafficSign trafficSign = TrafficSign.builder()
                .zoneCodeType(zoneCodeType)
                .build();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeTrafficSignZoneCodeTypes(Arrays.stream(ZoneCodeType.values())
                        .filter(zt1 -> zt1 != zoneCodeType)
                        .collect(Collectors.toSet())
                )
                .build();

        assertThat(excludeTrafficSignsWithZoneCodeTypes.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void test_accessibilityRequest_missingExcludeZoneCodeTypes() {

        TrafficSign trafficSign = TrafficSign.builder()
                .zoneCodeType(ZoneCodeType.END)
                .build();

        AccessibilityRequest accessibilityRequest = mock(AccessibilityRequest.class);
        when(accessibilityRequest.excludeTrafficSignZoneCodeTypes()).thenReturn(null);

        assertThat(excludeTrafficSignsWithZoneCodeTypes.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void test_trafficSign_missingZoneCodeType() {

        TrafficSign trafficSign = TrafficSign.builder().build();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeTrafficSignZoneCodeTypes(Set.of(ZoneCodeType.START))
                .build();

        assertThat(excludeTrafficSignsWithZoneCodeTypes.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void test_isRelevant_trafficSign_missingZoneCodeType_and_accessibilityRequest_missingExcludeZoneCodeTypes() {

        TrafficSign trafficSign = TrafficSign.builder().build();

        AccessibilityRequest accessibilityRequest = mock(AccessibilityRequest.class);
        when(accessibilityRequest.excludeTrafficSignZoneCodeTypes()).thenReturn(null);

        assertThat(excludeTrafficSignsWithZoneCodeTypes.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                excludeTrafficSignsWithZoneCodeTypes.getClass(),
                Component.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void implementsTrafficSignRelevancyInterface() {

        assertThat(excludeTrafficSignsWithZoneCodeTypes).isInstanceOf(TrafficSignExclusion.class);
    }
}