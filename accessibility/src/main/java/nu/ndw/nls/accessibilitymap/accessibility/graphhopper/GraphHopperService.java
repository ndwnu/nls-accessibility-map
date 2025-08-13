package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.storage.CHConfig;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.storage.RoutingCHGraphImpl;
import com.graphhopper.util.PMap;
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
    private RoutingCHGraph chGraph;

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
           // createChGraph();
            log.info("GraphHopper network loaded from disk in {}ms", Duration.between(start, OffsetDateTime.now()).toMillis());
        } catch (IOException | GraphHopperNotImportedException exception) {
            RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = graphHopperNetworkSettingsBuilder.defaultNetworkSettings();
            throw new IllegalStateException("Could not create network GraphHopper from %s"
                    .formatted(routingNetworkSettings.getGraphhopperRootPath().toAbsolutePath()), exception);
        }
    }

    public synchronized RoutingCHGraph getCHGraph() {
        if (Objects.isNull(chGraph)) {
            createNetworkGraphHopper();
        }
        return chGraph;
    }

    private void createChGraph() {
        CHConfig chConfig = CHConfig.nodeBased(NetworkConstants.CAR_PROFILE.getName() + "_ch",
                networkGraphHopper.createWeighting(NetworkConstants.CAR_PROFILE, new PMap()));
        networkGraphHopper.getBaseGraph().freeze();
        PrepareContractionHierarchies pch = PrepareContractionHierarchies.fromGraph(networkGraphHopper.getBaseGraph(), chConfig);
        PrepareContractionHierarchies.Result pchRes = pch.doWork();
        chGraph = RoutingCHGraphImpl.fromGraph(networkGraphHopper.getBaseGraph(), pchRes.getCHStorage(), pchRes.getCHConfig());
    }

}
