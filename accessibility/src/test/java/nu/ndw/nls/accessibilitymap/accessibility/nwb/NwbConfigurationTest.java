package nu.ndw.nls.accessibilitymap.accessibility.nwb;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.db.nwb.EnableNwbDataAccessServices;
import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ExtendWith(MockitoExtension.class)
class NwbConfigurationTest {

    @Test
    void class_routingMapMatcherConfigurationImportedAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                NwbConfiguration.class,
                Import.class,
                annotation -> assertThat(annotation.value()).containsExactlyInAnyOrder(RoutingMapMatcherConfiguration.class)
        );
    }

    @Test
    void class_enableNwbDataAccessServicesAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                NwbConfiguration.class,
                EnableNwbDataAccessServices.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                NwbConfiguration.class,
                Configuration.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void class_componentScanAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                NwbConfiguration.class,
                ComponentScan.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

}