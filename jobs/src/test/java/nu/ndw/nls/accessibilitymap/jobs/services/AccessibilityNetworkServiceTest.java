package nu.ndw.nls.accessibilitymap.jobs.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.jobs.services.AccessibilityLinkService.AccessibilityLinkData;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventSubject;
import nu.ndw.nls.events.NlsEventSubjectType;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import nu.ndw.nls.routingmapmatcher.domain.model.RoutingNetwork;
import nu.ndw.nls.routingmapmatcher.graphhopper.AccessibilityGraphHopperNetworkService;
import nu.ndw.nls.springboot.messaging.MessagePublisher;
import nu.ndw.nls.springboot.messaging.MessagePublisherFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityNetworkServiceTest {

    private static final String GRAPHHOPPER_DIR = "/tmp/graphhopper";
    private static final String NETWORK_NAME = "accessibility_latest";
    private static final int NWB_VERSION_ID = 20231001;
    private static final String NWB_VERSION_ID_STRING = "20231001";
    private static final String TRAFFIC_SIGN_TIMESTAMP_STRING = "2023-11-07T15:37:23Z";
    private static final Instant TRAFFIC_SIGN_TIMESTAMP = Instant.parse(TRAFFIC_SIGN_TIMESTAMP_STRING);

    @Mock
    private AccessibilityGraphHopperNetworkService accessibilityGraphHopperNetworkService;
    @Mock
    private AccessibilityLinkService accessibilityLinkService;
    @Mock
    private MessagePublisherFactory messagePublisherFactory;
    @Mock
    private MessagePublisher messagePublisher;

    private AccessibilityNetworkService accessibilityNetworkService;

    @Mock
    private List<Link> links;
    @Mock
    private Iterator<Link> linkIterator;
    @Captor
    private ArgumentCaptor<RoutingNetwork> routingNetworkArgumentCaptor;
    @Captor
    private ArgumentCaptor<NlsEvent> nlsEventArgumentCaptor;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        Files.deleteIfExists(Path.of(GRAPHHOPPER_DIR, NETWORK_NAME));
        Files.deleteIfExists(Path.of(GRAPHHOPPER_DIR));
        when(messagePublisherFactory.create("ndw.nls.accessibility.routing.network.updated",
                NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED)).thenReturn(messagePublisher);
        accessibilityNetworkService = new AccessibilityNetworkService(accessibilityGraphHopperNetworkService,
                accessibilityLinkService, messagePublisherFactory, GRAPHHOPPER_DIR);
    }

    @SneakyThrows
    @Test
    void storeLatestNetworkOnDisk_ok() {
        when(accessibilityLinkService.getLinks()).thenReturn(
                new AccessibilityLinkData(links, NWB_VERSION_ID, TRAFFIC_SIGN_TIMESTAMP));
        when(links.iterator()).thenReturn(linkIterator);

        accessibilityNetworkService.storeLatestNetworkOnDisk();

        verify(accessibilityGraphHopperNetworkService).storeOnDisk(routingNetworkArgumentCaptor.capture(),
                eq(Path.of(GRAPHHOPPER_DIR)));
        RoutingNetwork routingNetwork = routingNetworkArgumentCaptor.getValue();
        assertEquals(NETWORK_NAME, routingNetwork.getNetworkNameAndVersion());
        assertEquals(linkIterator, routingNetwork.getLinkSupplier().get());
        assertEquals(TRAFFIC_SIGN_TIMESTAMP, routingNetwork.getDataDate());
        assertTrue(Files.exists(Path.of(GRAPHHOPPER_DIR, NETWORK_NAME)));

        verify(messagePublisher).publishMessage(nlsEventArgumentCaptor.capture());
        NlsEvent nlsEvent = nlsEventArgumentCaptor.getValue();
        assertEquals(NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED, nlsEvent.getType());
        assertEquals(NlsEventSubject.builder()
                .type(NlsEventSubjectType.ACCESSIBILITY_ROUTING_NETWORK)
                .nwbVersion(NWB_VERSION_ID_STRING)
                .timestamp(TRAFFIC_SIGN_TIMESTAMP_STRING)
                .build(), nlsEvent.getSubject());
        assertNull(nlsEvent.getSourceEvent());
    }
}
