package nu.ndw.nls.accessibilitymap.accessibility.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperNetworkSettingsBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.AccessibilityRoutingNetworkEventMapper;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
import nu.ndw.nls.db.nwb.jooq.services.NwbVersionCrudService;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import nu.ndw.nls.springboot.core.time.ClockService;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccessibilityNetworkService {

    private final GraphHopperNetworkService graphHopperNetworkService;

    private final AccessibilityLinkService accessibilityLinkService;

    private final GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder;

    private final MessageService messageService;

    private final AccessibilityRoutingNetworkEventMapper accessibilityRoutingNetworkEventMapper;

    private final NetworkMetaDataService networkMetaDataService;

    private final NwbVersionCrudService nwbVersionCrudService;

    private final ClockService clockService;

    @Transactional
    public void storeLatestNetworkOnDisk() throws IOException {
        log.info("Starting network creation for {}", graphHopperNetworkSettingsBuilder.getLatestPath());

        Files.createDirectories(graphHopperNetworkSettingsBuilder.getLatestPath());

        int nwbVersionId = nwbVersionCrudService.findLatestVersionId();

        log.info("Retrieving link data");
        List<AccessibilityLink> accessibilityLinks = accessibilityLinkService.getLinks(nwbVersionId);
        Instant dataTimestamp = clockService.now().toInstant();

        RoutingNetworkSettings<AccessibilityLink> accessibilityLinkRoutingNetworkSettings =
                graphHopperNetworkSettingsBuilder.networkSettingsWithData(accessibilityLinks, dataTimestamp);

        log.info("Creating GraphHopper network and writing to disk");
        graphHopperNetworkService.storeOnDisk(accessibilityLinkRoutingNetworkSettings);

        networkMetaDataService.saveMetaData(new GraphhopperMetaData(nwbVersionId));

        if (graphHopperNetworkSettingsBuilder.publishEvents()) {
            NlsEvent nlsEvent = accessibilityRoutingNetworkEventMapper.map(nwbVersionId, dataTimestamp);
            log.info("Sending {} event for NWB version {}", nlsEvent.getType().getLabel(), nwbVersionId);
            messageService.publish(nlsEvent);
        }
    }

    protected boolean networkExists() {
        return Files.exists(graphHopperNetworkSettingsBuilder.getLatestPath());
    }

    @PostConstruct
    public void initializeNetwork() throws IOException {
        if (!networkExists()) {
            log.info("No network found on disk, creating new one");
            storeLatestNetworkOnDisk();
        } else {
            log.info("Network already exists on disk");
        }
    }
}
