package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.index.Snap;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.SnapRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphConfigurer;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util.Snapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityException;
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

    private final QueryGraphConfigurer queryGraphConfigurer;

    private final Snapper snapper;

    private final List<Runnable> newNetworkListeners;

    private final NetworkMetaDataService networkMetaDataService;

    @SuppressWarnings("java:S3749")
    private NetworkGraphHopper networkGraphHopper;

    public GraphHopperService(
            GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder,
            GraphHopperNetworkService graphHopperNetworkService,
            QueryGraphConfigurer queryGraphConfigurer,
            Snapper snapper,
            NetworkMetaDataService networkMetaDataService) {

        this.graphHopperNetworkSettingsBuilder = graphHopperNetworkSettingsBuilder;
        this.graphHopperNetworkService = graphHopperNetworkService;
        this.queryGraphConfigurer = queryGraphConfigurer;
        this.snapper = snapper;
        this.networkMetaDataService = networkMetaDataService;

        newNetworkListeners = new ArrayList<>();
    }

    public void registerUpdateListener(Runnable runnable) {
        synchronized (newNetworkListeners) {
            newNetworkListeners.add(runnable);
        }
    }

    public synchronized void loadNewNetworkGraphHopper() {
        try {
            RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = graphHopperNetworkSettingsBuilder.defaultNetworkSettings();
            Files.createDirectories(
                    routingNetworkSettings.getGraphhopperRootPath().resolve(Path.of(routingNetworkSettings.getNetworkNameAndVersion())));

            OffsetDateTime start = OffsetDateTime.now();

            networkGraphHopper = graphHopperNetworkService.loadFromDisk(routingNetworkSettings);
            log.info("GraphHopper network loaded from disk in {}ms", Duration.between(start, OffsetDateTime.now()).toMillis());
            newNetworkListeners.forEach(Runnable::run);
        } catch (IOException | GraphHopperNotImportedException exception) {
            RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = graphHopperNetworkSettingsBuilder.defaultNetworkSettings();
            throw new IllegalStateException(
                    "Could not load network GraphHopper from %s"
                            .formatted(routingNetworkSettings.getGraphhopperRootPath().toAbsolutePath()), exception);
        }
    }

    @SuppressWarnings("java:S1941")
    public @Valid GraphHopperNetwork getNetwork(Restrictions restrictions, Location from, Location destination) {
        NetworkGraphHopper localNetworkGraphHopper = getNetworkGraphHopper();

        Optional<Snap> fromSnap = snapper.snapLocation(localNetworkGraphHopper, from);
        if (fromSnap.isEmpty()) {
            throw new AccessibilityException("Could not find a snap point for from location (%s, %s).".formatted(
                    from.latitude(),
                    from.longitude()
            ));
        }
        Optional<Snap> destinationSnap = snapper.snapLocation(localNetworkGraphHopper, destination);
        List<SnapRestriction> snapRestrictions = restrictions.stream()
                .map(restriction -> snapper.snapRestriction(localNetworkGraphHopper, restriction)
                        .map(snap -> new SnapRestriction(snap, restriction)).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        List<Snap> snaps = Stream.of(
                        snapRestrictions.stream().map(SnapRestriction::snap),
                        Stream.of(fromSnap.get()),
                        destinationSnap.stream()
                )
                .flatMap(snapStream -> snapStream)
                .toList();

        QueryGraph queryGraph = QueryGraph.create(localNetworkGraphHopper.getBaseGraph(), snaps);

        return new GraphHopperNetwork(
                localNetworkGraphHopper,
                networkMetaDataService.loadMetaData().nwbVersion(),
                queryGraph,
                restrictions,
                queryGraphConfigurer.createEdgeRestrictions(queryGraph, snapRestrictions),
                fromSnap.get(),
                destinationSnap.orElse(null));
    }

    protected synchronized NetworkGraphHopper getNetworkGraphHopper() {

        if (Objects.isNull(networkGraphHopper)) {
            loadNewNetworkGraphHopper();
        }
        return networkGraphHopper;
    }
}
