package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphHopperService {

    private final GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder;

    private final GraphHopperNetworkService graphHopperNetworkService;

    private NetworkGraphHopper networkGraphHopper;

    public synchronized NetworkGraphHopper getNetworkGraphHopper() {

        if (Objects.isNull(networkGraphHopper)) {
            createNetworkGraphHopper();
        }
        return networkGraphHopper;
    }

    public synchronized void createNetworkGraphHopper() {
        try {
            RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = graphHopperNetworkSettingsBuilder.defaultNetworkSettings();
            Files.createDirectories(
                    routingNetworkSettings.getGraphhopperRootPath().resolve(Path.of(routingNetworkSettings.getNetworkNameAndVersion())));

            OffsetDateTime start = OffsetDateTime.now();
            networkGraphHopper = graphHopperNetworkService.loadFromDisk(routingNetworkSettings);
            log.info("GraphHopper network loaded from disk in {}ms", Duration.between(start, OffsetDateTime.now()).toMillis());
        } catch (IOException | GraphHopperNotImportedException exception) {
            throw new IllegalStateException("Could not create network graph hopper", exception);
        }
    }
}
