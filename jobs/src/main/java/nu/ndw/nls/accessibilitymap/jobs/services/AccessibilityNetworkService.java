package nu.ndw.nls.accessibilitymap.jobs.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.services.AccessibilityLinkService.AccessibilityLinkData;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventSubject;
import nu.ndw.nls.events.NlsEventSubjectType;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.routingmapmatcher.domain.model.RoutingNetwork;
import nu.ndw.nls.routingmapmatcher.graphhopper.IndexedGraphHopperNetworkService;
import nu.ndw.nls.springboot.messaging.MessagePublisher;
import nu.ndw.nls.springboot.messaging.MessagePublisherFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AccessibilityNetworkService {

    private static final String ROUTING_KEY = "ndw.nls.accessibility.routing.network.updated";
    private static final NlsEventType EVENT_TYPE = NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED;
    private static final String NETWORK_NAME = "accessibility_latest";

    private final IndexedGraphHopperNetworkService indexedGraphHopperNetworkService;
    private final AccessibilityLinkService accessibilityLinkService;
    private final MessagePublisher messagePublisher;
    private final Path graphHopperPath;

    public AccessibilityNetworkService(IndexedGraphHopperNetworkService indexedGraphHopperNetworkService,
            AccessibilityLinkService accessibilityLinkService, MessagePublisherFactory messagePublisherFactory,
            @Value("${graphhopper.dir}") String graphHopperDir) {
        this.indexedGraphHopperNetworkService = indexedGraphHopperNetworkService;
        this.accessibilityLinkService = accessibilityLinkService;
        this.messagePublisher = messagePublisherFactory.create(ROUTING_KEY, EVENT_TYPE);
        this.graphHopperPath = Path.of(graphHopperDir);
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

        log.debug("Sending " + EVENT_TYPE.getLabel() + " event for NWB version " + linkData.nwbVersionId()
                + " and traffic sign timestamp " + linkData.trafficSignTimestamp());
        sendNlsEvent(linkData.nwbVersionId(), linkData.trafficSignTimestamp());
    }

    private void sendNlsEvent(int nwbVersionId, Instant trafficSignTimestamp) {
        messagePublisher.publishMessage(NlsEvent.builder()
                .type(EVENT_TYPE)
                .subject(NlsEventSubject.builder()
                        .type(NlsEventSubjectType.ACCESSIBILITY_ROUTING_NETWORK)
                        .nwbVersion(String.valueOf(nwbVersionId))
                        .timestamp(trafficSignTimestamp.toString())
                        .build())
                .build());
    }
}
