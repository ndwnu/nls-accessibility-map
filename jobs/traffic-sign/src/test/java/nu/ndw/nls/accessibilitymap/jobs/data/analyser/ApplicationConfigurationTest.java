package nu.ndw.nls.accessibilitymap.jobs.data.analyser;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignClientConfiguration;
import nu.ndw.nls.geojson.geometry.JtsGeoJsonMappersConfiguration;
import nu.ndw.nls.locationdataissuesapi.client.feign.LocationDataIssuesApiClientConfiguration;
import nu.ndw.nls.springboot.datadog.DatadogConfiguration;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigurationTest {

    @Test
    void annotation_import() {

        AnnotationUtil.classContainsAnnotation(
                ApplicationConfiguration.class,
                Import.class,
                importAnnotation -> assertThat(importAnnotation.value()).containsExactlyInAnyOrder(
                        AccessibilityConfiguration.class,
                        DatadogConfiguration.class,
                        TrafficSignClientConfiguration.class,
                        LocationDataIssuesApiClientConfiguration.class,
                        JtsGeoJsonMappersConfiguration.class));
    }
}
