package nu.ndw.nls.accessibilitymap.jobs.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapper.AccessibilityRoutingNetworkEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.services.AccessibilityLinkService.AccessibilityLinkData;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.routingmapmatcher.domain.model.RoutingNetwork;
import nu.ndw.nls.routingmapmatcher.graphhopper.IndexedGraphHopperNetworkService;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AccessibilityNetworkService {
    private static final String NETWORK_NAME = "accessibility_latest";

    private final IndexedGraphHopperNetworkService indexedGraphHopperNetworkService;
    private final AccessibilityLinkService accessibilityLinkService;
    private final Path graphHopperPath;
    private final MessageService messageService;
    private final AccessibilityRoutingNetworkEventMapper accessibilityRoutingNetworkEventMapper;

    public AccessibilityNetworkService(IndexedGraphHopperNetworkService indexedGraphHopperNetworkService,
            AccessibilityLinkService accessibilityLinkService,
            @Value("${graphhopper.dir}") String graphHopperDir,
            MessageService messageService,
            AccessibilityRoutingNetworkEventMapper accessibilityRoutingNetworkEventMapper) {
        this.indexedGraphHopperNetworkService = indexedGraphHopperNetworkService;
        this.accessibilityLinkService = accessibilityLinkService;
        this.graphHopperPath = Path.of(graphHopperDir);
        this.messageService = messageService;
        this.accessibilityRoutingNetworkEventMapper = accessibilityRoutingNetworkEventMapper;
    }

    @Transactional
    public void storeLatestNetworkOnDisk() throws IOException {
        log.debug("Starting network creation for " + NETWORK_NAME);
        Files.createDirectories(graphHopperPath.resolve(NETWORK_NAME));

        log.debug("Retrieving link data");
        AccessibilityLinkData linkData = accessibilityLinkService.getLinks();
        var routingNetwork = RoutingNetwork.builder()
                .networkNameAndVersion(NETWORK_NAME)
                .linkSupplier(() -> linkData.links().iterator())
                .dataDate(linkData.trafficSignTimestamp())
                .build();

        log.debug("Creating GraphHopper network and writing to disk");
        indexedGraphHopperNetworkService.storeOnDisk(routingNetwork, graphHopperPath);

        NlsEvent nlsEvent = accessibilityRoutingNetworkEventMapper.map(linkData.nwbVersionId(),
                linkData.trafficSignTimestamp());

        log.debug("Sending " + nlsEvent.getType().getLabel() + " event for NWB version " + linkData.nwbVersionId()
                + " and traffic sign timestamp " + linkData.trafficSignTimestamp());
        messageService.publish(nlsEvent);
    }

}
