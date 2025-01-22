package nu.ndw.nls.accessibilitymap.accessibility.core.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class ClockBeanConfigurationTest {

    private ClockBeanConfiguration clockBeanConfiguration;

    @BeforeEach
    void setUp() {

        clockBeanConfiguration = new ClockBeanConfiguration();
    }

    @Test
    void clock_ok() {

        assertThat(clockBeanConfiguration.clock()).isEqualTo(Clock.systemUTC());
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                clockBeanConfiguration.getClass(),
                Configuration.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void clock_beanAnnotation() {

        AnnotationUtil.methodContainsAnnotation(
                clockBeanConfiguration.getClass(),
                Bean.class,
                "clock",
                annotation -> assertThat(annotation).isNotNull()
        );
    }
}
