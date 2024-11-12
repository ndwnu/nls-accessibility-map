package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.utils.AnnotationUtil;
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
    void clock() {

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

        AnnotationUtil.methodsContainsAnnotation(
                clockBeanConfiguration.getClass(),
                Bean.class,
                "clock",
                annotation -> assertThat(annotation).isNotNull()
        );
    }
}
