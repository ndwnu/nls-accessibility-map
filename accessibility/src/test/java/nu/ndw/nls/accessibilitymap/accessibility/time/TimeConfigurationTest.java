package nu.ndw.nls.accessibilitymap.accessibility.time;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ExtendWith(MockitoExtension.class)
class TimeConfigurationTest {

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                TimeConfiguration.class,
                Configuration.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void class_componentScanAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                TimeConfiguration.class,
                ComponentScan.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }
}