package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.exclude;

import static org.assertj.core.api.Assertions.assertThat;

import com.graphhopper.util.shapes.BBox;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.stereotype.Component;

class ExcludeTrafficSignsThatAreOutsideOfBoundingBoxTest {

    private ExcludeTrafficSignsThatAreOutsideOfBoundingBox excludeTrafficSignsThatAreOutsideOfBoundingBox;

    private TrafficSign trafficSign;

    @BeforeEach
    void setUp() {

        excludeTrafficSignsThatAreOutsideOfBoundingBox = new ExcludeTrafficSignsThatAreOutsideOfBoundingBox();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            1.0, 2.0, false
            3.0, 4.0, false
            0.9, 2.0, true
            1.0, 4.1, true
            0.9, 4.1, true
            """)
    void test(double lat, double lon, boolean isOutsideOfBoundingBox) {

        trafficSign = TrafficSign.builder()
                .latitude(lat)
                .longitude(lon)
                .build();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .boundingBox(BBox.fromPoints(1.0, 2.0, 3.0, 4.0))
                .build();

        assertThat(excludeTrafficSignsThatAreOutsideOfBoundingBox.test(trafficSign, accessibilityRequest)).isEqualTo(isOutsideOfBoundingBox);
    }

    @Test
    void test_isNotExcluding_accessibilityRequest_boundingBox_notSet() {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder().build();

        assertThat(excludeTrafficSignsThatAreOutsideOfBoundingBox.test(trafficSign, accessibilityRequest)).isFalse();
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                excludeTrafficSignsThatAreOutsideOfBoundingBox.getClass(),
                Component.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void implementsTrafficSignRelevancyInterface() {

        assertThat(excludeTrafficSignsThatAreOutsideOfBoundingBox).isInstanceOf(TrafficSignExclusion.class);
    }
}