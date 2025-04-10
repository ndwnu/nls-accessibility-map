package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
import nu.ndw.nls.geometry.GeometryConfiguration;
import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ComponentScan
@Configuration
@RequiredArgsConstructor
@Import({GeometryConfiguration.class, RoutingMapMatcherConfiguration.class})
@ConditionalOnProperty(name = "graphhopper.enabled", havingValue = "true", matchIfMissing = true)
public class GraphhopperConfiguration {

    private final GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder;
    private final GraphHopperNetworkService graphHopperNetworkService;
    private final NetworkMetaDataService networkMetaDataService;

    @Bean
    public GraphhopperMetaData getMetaData() {
        return networkMetaDataService.loadMetaData();
    }

    @Bean
    public NetworkGraphHopper networkGraphHopper() throws GraphHopperNotImportedException {
        return graphHopperNetworkService
                .loadFromDisk(graphHopperNetworkSettingsBuilder.buildDefaultNetworkSettings());
    }

    @Bean
    public EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor() {
        return new EdgeIteratorStateReverseExtractor();
    }

    @Bean
    public EncodingManager encodingManger(NetworkGraphHopper networkGraphHopper) {
        return networkGraphHopper.getEncodingManager();
    }
}
