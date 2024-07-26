package nu.ndw.nls.accessibilitymap.jobs.services;

import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapper.AccessibilityRoutingNetworkEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.services.AccessibilityLinkService.AccessibilityLinkData;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperConfiguration;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccessibilityNetworkService {

    private final GraphHopperNetworkService graphHopperNetworkService;
    private final AccessibilityLinkService accessibilityLinkService;
    private final GraphHopperConfiguration graphHopperConfiguration;
    private final MessageService messageService;
    private final AccessibilityRoutingNetworkEventMapper accessibilityRoutingNetworkEventMapper;

    @Transactional
    public void storeLatestNetworkOnDisk() throws IOException {
        log.debug("Starting network creation for {}", graphHopperConfiguration.getLatestPath());

        Files.createDirectories(graphHopperConfiguration.getLatestPath());

        log.debug("Retrieving link data");
        AccessibilityLinkData linkData = accessibilityLinkService.getLinks();

        RoutingNetworkSettings<AccessibilityLink> accessibilityLinkRoutingNetworkSettings =
                graphHopperConfiguration.configurePersistingRoutingNetworkSettings(
                () -> linkData.links().iterator(),
                linkData.trafficSignTimestamp());

        log.debug("Creating GraphHopper network and writing to disk");
        graphHopperNetworkService.storeOnDisk(accessibilityLinkRoutingNetworkSettings);

        int nwbVersionId = linkData.nwbVersionId();

        NlsEvent nlsEvent = accessibilityRoutingNetworkEventMapper.map(nwbVersionId, linkData.trafficSignTimestamp());

        log.debug("Sending {} event for NWB version {} and traffic sign timestamp {}",
                nlsEvent.getType().getLabel(), linkData.nwbVersionId(), linkData.trafficSignTimestamp());
        messageService.publish(nlsEvent);
    }

}
