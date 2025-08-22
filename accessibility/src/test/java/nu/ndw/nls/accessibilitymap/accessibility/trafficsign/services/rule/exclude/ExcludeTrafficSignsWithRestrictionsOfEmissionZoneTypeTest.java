package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.exclude;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExcludeTrafficSignsWithRestrictionsOfEmissionZoneTypeTest {

    private ExcludeTrafficSignsWithRestrictionsOfEmissionZoneType excludeTrafficSignsWithRestrictionsOfEmissionZoneType;

    @BeforeEach
    void setUp() {

        excludeTrafficSignsWithRestrictionsOfEmissionZoneType = new ExcludeTrafficSignsWithRestrictionsOfEmissionZoneType();
    }

    @Test
    void test() {

        TrafficSign trafficSign = TrafficSign.builder()
                .restrictions(Restrictions.builder()
                        .emissionZone(EmissionZone.builder().type(EmissionZoneType.LOW).build())
                        .build())
                .build();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeRestrictionsWithEmissionZoneTypes(Set.of(EmissionZoneType.LOW))
                .build();

        assertThat(excludeTrafficSignsWithRestrictionsOfEmissionZoneType.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @Test
    void test_accessibilityRequest_missingExcludeRestrictionsWithEmissionZoneTypes() {

        TrafficSign trafficSign = TrafficSign.builder()
                .restrictions(Restrictions.builder()
                        .emissionZone(EmissionZone.builder().type(EmissionZoneType.LOW).build())
                        .build())
                .build();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder().build();

        assertThat(excludeTrafficSignsWithRestrictionsOfEmissionZoneType.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void test_trafficSign_noEmissionZone() {

        TrafficSign trafficSign = TrafficSign.builder()
                .restrictions(Restrictions.builder().build())
                .build();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .excludeRestrictionsWithEmissionZoneTypes(Set.of(EmissionZoneType.LOW))
                .build();

        assertThat(excludeTrafficSignsWithRestrictionsOfEmissionZoneType.test(trafficSign, accessibilityRequest)).isFalse();
    }
}