package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
import nu.ndw.nls.geometry.GeometryConfiguration;
import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ExtendWith(MockitoExtension.class)
class GraphhopperWriteConfigurationTest {

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                GraphhopperWriteConfiguration.class,
                Configuration.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void class_importAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                GraphhopperWriteConfiguration.class,
                Import.class,
                annotation -> assertThat(annotation.value()).containsExactlyInAnyOrder(
                        GeometryConfiguration.class, RoutingMapMatcherConfiguration.class,
                        GraphHopperNetworkSettingsBuilder.class, GraphHopperNetworkService.class, NetworkMetaDataService.class
                )
        );
    }
}