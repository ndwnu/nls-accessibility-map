package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GraphHopperService {

    private final GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder;

    private final GraphHopperNetworkService graphHopperNetworkService;

    private final List<Runnable> newNetworkListeners;

    private final NetworkMetaDataService networkMetaDataService;

    @SuppressWarnings("java:S3749")
    private GraphHopperNetwork graphHopperNetwork;

    public GraphHopperService(
            GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder,
            GraphHopperNetworkService graphHopperNetworkService,
            NetworkMetaDataService networkMetaDataService) {

        this.graphHopperNetworkSettingsBuilder = graphHopperNetworkSettingsBuilder;
        this.graphHopperNetworkService = graphHopperNetworkService;
        this.networkMetaDataService = networkMetaDataService;

        newNetworkListeners = new ArrayList<>();
    }

    public void registerUpdateListener(Runnable runnable) {
        synchronized (newNetworkListeners) {
            newNetworkListeners.add(runnable);
        }
    }

    public synchronized void loadNewGraphHopperNetwork() {
        try {
            RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = graphHopperNetworkSettingsBuilder.defaultNetworkSettings();
            Files.createDirectories(
                    routingNetworkSettings.getGraphhopperRootPath().resolve(Path.of(routingNetworkSettings.getNetworkNameAndVersion())));

            OffsetDateTime start = OffsetDateTime.now();

            NetworkGraphHopper networkGraphHopper = graphHopperNetworkService.loadFromDisk(routingNetworkSettings);
            int nwbVersionId = networkMetaDataService.loadMetaData().nwbVersion();
            graphHopperNetwork = GraphHopperNetwork.builder()
                    .network(networkGraphHopper)
                    .nwbVersion(nwbVersionId)
                    .build();
            log.info("GraphHopper network loaded from disk in {}ms", Duration.between(start, OffsetDateTime.now()).toMillis());
            newNetworkListeners.forEach(Runnable::run);
        } catch (IOException | GraphHopperNotImportedException exception) {
            RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = graphHopperNetworkSettingsBuilder.defaultNetworkSettings();
            throw new IllegalStateException(
                    "Could not load network GraphHopper from %s"
                            .formatted(routingNetworkSettings.getGraphhopperRootPath().toAbsolutePath()), exception);
        }
    }

    public synchronized GraphHopperNetwork getNetworkGraphHopper() {

        if (Objects.isNull(graphHopperNetwork)) {
            loadNewGraphHopperNetwork();
        }
        return graphHopperNetwork;
    }
}
