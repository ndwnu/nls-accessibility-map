package nu.ndw.nls.accessibilitymap.backend;

import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.shared.SharedConfiguration;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.shared.network.services.NetworkMetaDataService;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperConfiguration;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperProperties;
import nu.ndw.nls.geometry.GeometryConfiguration;
import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.datadog.DatadogConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@RequiredArgsConstructor
@Import({DatadogConfiguration.class, GeometryConfiguration.class, RoutingMapMatcherConfiguration.class,
        SharedConfiguration.class})
@EnableConfigurationProperties(GraphHopperProperties.class)
public class AccessibilityMapConfiguration {

    private final GraphHopperConfiguration graphHopperConfiguration;
    private final GraphHopperNetworkService graphHopperNetworkService;
    private final NetworkMetaDataService networkMetaDataService;

    @Bean
    public AccessibilityGraphhopperMetaData accessibilityGraphhopperMetaData() {
        return networkMetaDataService.loadMetaData();
    }

    @Bean
    public NetworkGraphHopper networkGraphHopper() throws GraphHopperNotImportedException {
        return graphHopperNetworkService
                .loadFromDisk(graphHopperConfiguration.configureLoadingRoutingNetworkSettings());
    }

    @Bean
    public EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor() {
        return new EdgeIteratorStateReverseExtractor();
    }
}
