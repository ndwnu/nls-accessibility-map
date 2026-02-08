package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.CAR_PROFILE;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.annotation.Timed;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.AccessibilityNwbRoadSectionToLinkMapper;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings.RoutingNetworkSettingsBuilder;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GraphHopperService {

    private static final String ACCESSIBILITY_METADATA_JSON = "accessibility_metadata.json";

    private static final String GRAPH_HOPPER_NETWORK_NAME = "latest";

    private final GraphHopperNetworkService graphHopperNetworkService;

    private final AccessibilityNwbRoadSectionToLinkMapper accessibilityNwbRoadSectionToLinkMapper;

    private final ObjectMapper objectMapper;

    private final ClockService clockService;

    @Timed(value = "accessibilitymap.graphHopper.load")
    public GraphHopperNetwork load(Path location) {
        RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = defaultNetworkSettings(location);
        try {
            OffsetDateTime start = clockService.now();

            NetworkGraphHopper networkGraphHopper = graphHopperNetworkService.loadFromDisk(routingNetworkSettings);

            GraphHopperNetwork graphHopperNetwork = GraphHopperNetwork.builder()
                    .network(networkGraphHopper)
                    .nwbVersion(loadMetaData(location).nwbVersion())
                    .build();
            log.info("GraphHopper network loaded from disk in {}ms", Duration.between(start, clockService.now()).toMillis());

            return graphHopperNetwork;
        } catch (GraphHopperNotImportedException exception) {
            throw new IllegalStateException(
                    "Could not load GraphHopper network from %s"
                            .formatted(routingNetworkSettings.getGraphhopperRootPath().toAbsolutePath()), exception);
        }
    }

    @Timed(value = "accessibilitymap.graphHopper.save")
    public void save(Path location, NwbData nwbData) throws IOException {
        OffsetDateTime start = clockService.now();

        Path graphHopperLocation = location.resolve(GRAPH_HOPPER_NETWORK_NAME).toAbsolutePath();
        Files.createDirectories(graphHopperLocation);

        List<AccessibilityLink> accessibilityLinks = nwbData.findAllAccessibilityNwbRoadSections().stream()
                .map(accessibilityNwbRoadSectionToLinkMapper::map)
                .toList();

        var accessibilityLinkRoutingNetworkSettings = networkSettingsWithData(location, accessibilityLinks, start);

        graphHopperNetworkService.storeOnDisk(accessibilityLinkRoutingNetworkSettings);
        saveMetaData(location, nwbData.getNwbVersionId());

        log.info("GraphHopper network stored on disk in {}ms", Duration.between(start, clockService.now()).toMillis());
    }

    private RoutingNetworkSettings<AccessibilityLink> defaultNetworkSettings(Path location) {

        return defaultNetworkSettingsBuilder(location).build();
    }

    private RoutingNetworkSettings<AccessibilityLink> networkSettingsWithData(
            Path location,
            List<AccessibilityLink> accessibilityLinks,
            OffsetDateTime timestamp) {

        return defaultNetworkSettingsBuilder(location)
                .indexed(true)
                .linkSupplier(accessibilityLinks::iterator)
                .dataDate(timestamp.toInstant())
                .build();
    }

    private RoutingNetworkSettingsBuilder<AccessibilityLink> defaultNetworkSettingsBuilder(Path location) {

        return RoutingNetworkSettings
                .builder(AccessibilityLink.class)
                .indexed(true)
                .graphhopperRootPath(location)
                .networkNameAndVersion(GRAPH_HOPPER_NETWORK_NAME)
                .profiles(List.of(CAR_PROFILE));
    }

    private GraphhopperMetaData loadMetaData(Path location) {
        Path metaDataLocation = location.resolve(GRAPH_HOPPER_NETWORK_NAME).resolve(ACCESSIBILITY_METADATA_JSON);
        try {
            return objectMapper.readValue(metaDataLocation.toFile(), GraphhopperMetaData.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load meta-data from file path: %s".formatted(metaDataLocation), exception);
        }
    }

    private void saveMetaData(Path location, int nwbVersionId) {
        Path metaDataLocation = location.resolve(GRAPH_HOPPER_NETWORK_NAME).resolve(ACCESSIBILITY_METADATA_JSON);
        try {
            objectMapper.writeValue(metaDataLocation.toFile(), new GraphhopperMetaData(nwbVersionId));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write meta-data to file path: %s".formatted(metaDataLocation), exception);
        }
    }
}
