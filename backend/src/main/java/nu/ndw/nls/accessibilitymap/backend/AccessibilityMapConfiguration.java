package nu.ndw.nls.accessibilitymap.backend;

import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.NETWORK_NAME;
import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.PROFILE;

import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.shared.SharedConfiguration;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperProperties;
import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import nu.ndw.nls.routingmapmatcher.util.CrsTransformer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@RequiredArgsConstructor
@Import({RoutingMapMatcherConfiguration.class,  SharedConfiguration.class})
@EnableConfigurationProperties(GraphHopperProperties.class)
public class AccessibilityMapConfiguration {

    private final GraphHopperProperties graphHopperProperties;
    private final GraphHopperNetworkService graphHopperNetworkService;

    @Bean
    public NetworkGraphHopper networkGraphHopper() throws GraphHopperNotImportedException {

        RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = RoutingNetworkSettings
                .builder(AccessibilityLink.class)
                .profiles(List.of(PROFILE))
                .graphhopperRootPath(graphHopperProperties.getDir())
                .networkNameAndVersion(NETWORK_NAME)
                .build();

        return graphHopperNetworkService.loadFromDisk(routingNetworkSettings);

    }

    @Bean
    public CrsTransformer crsTransformer() {
        return new CrsTransformer();
    }

    @Bean
    public EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor() {
        return new EdgeIteratorStateReverseExtractor();
    }

}
