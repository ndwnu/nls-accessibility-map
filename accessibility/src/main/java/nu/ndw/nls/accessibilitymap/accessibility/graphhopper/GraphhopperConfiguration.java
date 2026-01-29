package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.RoutingAlgorithmFactorySimple;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.geometry.GeometryConfiguration;
import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ComponentScan
@Configuration
@RequiredArgsConstructor
@Import({GeometryConfiguration.class, RoutingMapMatcherConfiguration.class})
@Slf4j
public class GraphhopperConfiguration {

    @Bean
    public EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor() {

        return new EdgeIteratorStateReverseExtractor();
    }

    @Bean
    public RoutingAlgorithmFactory algorithmFactory() {

        return new RoutingAlgorithmFactorySimple();
    }
}
