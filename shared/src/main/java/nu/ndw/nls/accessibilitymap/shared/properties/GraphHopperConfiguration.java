package nu.ndw.nls.accessibilitymap.shared.properties;


import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.PROFILE;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings.RoutingNetworkSettingsBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(GraphHopperProperties.class)
public class GraphHopperConfiguration {

    private final GraphHopperProperties graphHopperProperties;

    public Path getLatestPath() {
        return graphHopperProperties.getLatestPath();
    }

    public Path getMetaDataPath() {
        return graphHopperProperties.getMetaDataPath();
    }

    public RoutingNetworkSettings<AccessibilityLink> configureLoadingRoutingNetworkSettings() {
        return configureBaseRoutingNetworkSettingsBuilder().build();
    }

    public RoutingNetworkSettings<AccessibilityLink> configurePersistingRoutingNetworkSettings(
            Supplier<Iterator<AccessibilityLink>> accessibilityLinkSupplier, Instant trafficSignData) {
        return configureBaseRoutingNetworkSettingsBuilder()
                .indexed(true)
                .linkSupplier(accessibilityLinkSupplier)
                .dataDate(trafficSignData)
                .build();
    }

    private RoutingNetworkSettingsBuilder<AccessibilityLink> configureBaseRoutingNetworkSettingsBuilder() {
        return RoutingNetworkSettings
                .builder(AccessibilityLink.class)
                .indexed(true)
                .graphhopperRootPath(graphHopperProperties.getDir())
                .networkNameAndVersion(graphHopperProperties.getNetworkName())
                .profiles(List.of(PROFILE));
    }

    public boolean publishEvents() {
        return graphHopperProperties.isPublishEvents();
    }

}
