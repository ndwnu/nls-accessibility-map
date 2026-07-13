package nu.ndw.nls.accessibilitymap.accessibility.speedlimit.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfigurationTest;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ExtendWith(MockitoExtension.class)
class SpeedLimitCacheConfigurationTest  extends CacheConfigurationTest {

    @Override
    protected Class<?> getClassToTest() {
        return SpeedLimitCacheConfiguration.class;
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                getClassToTest(),
                Configuration.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void class_configurationPropertiesAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                getClassToTest(),
                ConfigurationProperties.class,
                annotation -> assertThat(annotation.prefix()).isEqualTo("nu.ndw.nls.accessibilitymap.speed-limit.cache")
        );
    }
}
