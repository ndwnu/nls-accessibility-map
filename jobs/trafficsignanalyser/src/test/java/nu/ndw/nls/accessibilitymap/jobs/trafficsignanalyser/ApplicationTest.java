package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.geojson.geometry.JtsGeoJsonMappersConfiguration;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

@ExtendWith(MockitoExtension.class)
class ApplicationTest {

    @Test
    void annotation_hasJtsGeoJsonMappersConfigurationConfigured() {

        AnnotationUtil.classContainsAnnotation(
                Application.class,
                Import.class,
                importAnnotation -> assertThat(importAnnotation.value()).containsExactly(JtsGeoJsonMappersConfiguration.class));
    }
}