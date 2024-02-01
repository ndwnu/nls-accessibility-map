package nu.ndw.nls.accessibilitymap.jobs.services;

import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.NETWORK_NAME;
import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.PROFILE;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapper.AccessibilityRoutingNetworkEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.services.AccessibilityLinkService.AccessibilityLinkData;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperProperties;
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
    private final GraphHopperProperties graphHopperProperties;
    private final MessageService messageService;
    private final AccessibilityRoutingNetworkEventMapper accessibilityRoutingNetworkEventMapper;

    @Transactional
    public void storeLatestNetworkOnDisk() throws IOException {
        log.debug("Starting network creation for {}", NETWORK_NAME);
        Files.createDirectories(graphHopperProperties.getDir().resolve(NETWORK_NAME));

        log.debug("Retrieving link data");
        AccessibilityLinkData linkData = accessibilityLinkService.getLinks();
        var routingNetworkSettings = RoutingNetworkSettings.builder(AccessibilityLink.class)
                .networkNameAndVersion(NETWORK_NAME)
                .profiles(List.of(PROFILE))
                .graphhopperRootPath(graphHopperProperties.getDir())
                .linkSupplier(() -> linkData.links().iterator())
                .dataDate(linkData.trafficSignTimestamp())
                .indexed(true)
                .build();

        log.debug("Creating GraphHopper network and writing to disk");
        graphHopperNetworkService.storeOnDisk(routingNetworkSettings);

        NlsEvent nlsEvent = accessibilityRoutingNetworkEventMapper.map(linkData.nwbVersionId(),
                linkData.trafficSignTimestamp());

        log.debug("Sending {} event for NWB version {} and traffic sign timestamp {}",
                nlsEvent.getType().getLabel(), linkData.nwbVersionId(), linkData.trafficSignTimestamp());
        messageService.publish(nlsEvent);
    }

}
