package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.relevance;

import static org.assertj.core.api.Assertions.assertThat;

import com.graphhopper.util.shapes.BBox;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;

@ExtendWith(MockitoExtension.class)
class IsWithinBoundingBoxTest {

    private IsWithinBoundingBox isWithinBoundingBox;

    private TrafficSign trafficSign;

    @BeforeEach
    void setUp() {

        isWithinBoundingBox = new IsWithinBoundingBox();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            1.0, 2.0, true
            3.0, 4.0, true
            0.9, 2.0, false
            1.0, 4.1, false
            0.9, 4.1, false
            """)
    void test(double lat, double lon, boolean expected) {

        trafficSign = TrafficSign.builder()
                .latitude(lat)
                .longitude(lon)
                .build();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .boundingBox(BBox.fromPoints(1.0, 2.0, 3.0, 4.0))
                .build();

        assertThat(isWithinBoundingBox.test(trafficSign, accessibilityRequest)).isEqualTo(expected);
    }

    @Test
    void test_isRelevant_accessibilityRequest_boundingBox() {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder().build();

        assertThat(isWithinBoundingBox.test(trafficSign, accessibilityRequest)).isTrue();
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                isWithinBoundingBox.getClass(),
                Component.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void implementsTrafficSignRelevancyInterface() {

        assertThat(isWithinBoundingBox).isInstanceOf(TrafficSignRelevancy.class);
    }
}