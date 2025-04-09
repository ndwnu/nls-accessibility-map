package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.PROFILE;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.configuration.GraphHopperProperties;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings.RoutingNetworkSettingsBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(GraphHopperProperties.class)
public class GraphHopperNetworkSettingsBuilder {

    private final GraphHopperProperties graphHopperProperties;

    public Path getLatestPath() {
        return graphHopperProperties.getLatestPath();
    }

    public Path getMetaDataPath() {
        return graphHopperProperties.getMetaDataPath();
    }

    public RoutingNetworkSettings<AccessibilityLink> buildDefaultNetworkSettings() {
        return defaultNetworkSettingsBuilder().build();
    }

    public RoutingNetworkSettings<AccessibilityLink> buildNetworkSettingsWithData(
            List<AccessibilityLink> accessibilityLinks,
            Instant dataTimestamp) {

        return defaultNetworkSettingsBuilder()
                .indexed(true)
                .linkSupplier(accessibilityLinks::iterator)
                .dataDate(dataTimestamp)
                .build();
    }

    private RoutingNetworkSettingsBuilder<AccessibilityLink> defaultNetworkSettingsBuilder() {
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
