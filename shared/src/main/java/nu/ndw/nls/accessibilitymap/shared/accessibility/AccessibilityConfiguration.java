package nu.ndw.nls.accessibilitymap.shared.accessibility;

import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.shared.network.services.NetworkMetaDataService;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperConfiguration;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AccessibilityConfiguration {

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
