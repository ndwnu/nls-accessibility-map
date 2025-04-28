package nu.ndw.nls.accessibilitymap.accessibility.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ExtendWith(MockitoExtension.class)
class TrafficSignDataConfigurationTest {

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                TrafficSignDataConfiguration.class,
                Configuration.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void class_componentScanAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                TrafficSignDataConfiguration.class,
                ComponentScan.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }
}