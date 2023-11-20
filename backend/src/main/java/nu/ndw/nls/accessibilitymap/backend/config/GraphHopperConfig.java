package nu.ndw.nls.accessibilitymap.backend.config;

import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.routingmapmatcher.domain.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.domain.model.RoutingNetwork;
import nu.ndw.nls.routingmapmatcher.graphhopper.AccessibilityGraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.graphhopper.NetworkGraphHopper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GraphHopperConfig {

    private static final String NETWORK_NAME = "accessibility_latest";
    private @Value("${graphhopper.dir}") String graphHopperDir;
    private final AccessibilityGraphHopperNetworkService accessibilityGraphHopperNetworkService;

    @Bean
    public NetworkGraphHopper networkGraphHopper() throws GraphHopperNotImportedException {
        RoutingNetwork routingNetwork = RoutingNetwork
                .builder()
                .networkNameAndVersion(NETWORK_NAME)
                .build();
        return accessibilityGraphHopperNetworkService.loadFromDisk(routingNetwork,
                Path.of(graphHopperDir));
    }

}
