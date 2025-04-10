package nu.ndw.nls.accessibilitymap.accessibility;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

class AccessibilityConfigurationTest {

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                AccessibilityConfiguration.class,
                Configuration.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void class_componentScanAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                AccessibilityConfiguration.class,
                ComponentScan.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

}