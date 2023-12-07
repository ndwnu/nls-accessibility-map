package nu.ndw.nls.accessibilitymap.jobs.services;

import static nu.ndw.nls.events.NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import nu.ndw.nls.accessibilitymap.jobs.mapper.AccessibilityRoutingNetworkEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.services.AccessibilityLinkService.AccessibilityLinkData;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import nu.ndw.nls.routingmapmatcher.domain.model.RoutingNetwork;
import nu.ndw.nls.routingmapmatcher.graphhopper.IndexedGraphHopperNetworkService;
import nu.ndw.nls.springboot.messaging.services.MessageService;
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
    private static final String TRAFFIC_SIGN_TIMESTAMP_STRING = "2023-11-07T15:37:23Z";
    private static final Instant TRAFFIC_SIGN_TIMESTAMP = Instant.parse(TRAFFIC_SIGN_TIMESTAMP_STRING);

    @Mock
    private IndexedGraphHopperNetworkService indexedGraphHopperNetworkService;
    @Mock
    private AccessibilityLinkService accessibilityLinkService;

    private AccessibilityNetworkService accessibilityNetworkService;

    @Mock
    private List<Link> links;
    @Mock
    private Iterator<Link> linkIterator;
    @Mock
    private MessageService messageService;
    @Mock
    private NlsEvent publishEvent;
    @Mock
    private AccessibilityRoutingNetworkEventMapper accessibilityRoutingNetworkEventMapper;

    @Captor
    private ArgumentCaptor<RoutingNetwork> routingNetworkArgumentCaptor;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        Files.deleteIfExists(Path.of(GRAPHHOPPER_DIR, NETWORK_NAME));
        Files.deleteIfExists(Path.of(GRAPHHOPPER_DIR));

        accessibilityNetworkService = new AccessibilityNetworkService(indexedGraphHopperNetworkService,
                accessibilityLinkService, GRAPHHOPPER_DIR,
                messageService, accessibilityRoutingNetworkEventMapper);
    }

    @SneakyThrows
    @Test
    void storeLatestNetworkOnDisk_ok() {
        when(accessibilityLinkService.getLinks()).thenReturn(
                new AccessibilityLinkData(links, NWB_VERSION_ID, TRAFFIC_SIGN_TIMESTAMP));
        when(links.iterator()).thenReturn(linkIterator);

        when(accessibilityRoutingNetworkEventMapper.map(NWB_VERSION_ID, TRAFFIC_SIGN_TIMESTAMP))
                .thenReturn(publishEvent);
        when(publishEvent.getType()).thenReturn(ACCESSIBILITY_ROUTING_NETWORK_UPDATED);

        accessibilityNetworkService.storeLatestNetworkOnDisk();

        verify(indexedGraphHopperNetworkService).storeOnDisk(routingNetworkArgumentCaptor.capture(),
                eq(Path.of(GRAPHHOPPER_DIR)));
        RoutingNetwork routingNetwork = routingNetworkArgumentCaptor.getValue();
        assertEquals(NETWORK_NAME, routingNetwork.getNetworkNameAndVersion());
        assertEquals(linkIterator, routingNetwork.getLinkSupplier().get());
        assertEquals(TRAFFIC_SIGN_TIMESTAMP, routingNetwork.getDataDate());
        assertTrue(Files.exists(Path.of(GRAPHHOPPER_DIR, NETWORK_NAME)));

        verify(messageService).publish(publishEvent);
    }
}
