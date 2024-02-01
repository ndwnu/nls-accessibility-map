package nu.ndw.nls.accessibilitymap.jobs.services;

import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.NETWORK_NAME;
import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.PROFILE;
import static nu.ndw.nls.events.NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperProperties;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityNetworkServiceTest {
    private static final String GRAPHHOPPER_DIR = "/tmp/graphhopper";
    private static final int NWB_VERSION_ID = 20231001;
    private static final String TRAFFIC_SIGN_TIMESTAMP_STRING = "2023-11-07T15:37:23Z";
    private static final Instant TRAFFIC_SIGN_TIMESTAMP = Instant.parse(TRAFFIC_SIGN_TIMESTAMP_STRING);


    @Mock
    private List<AccessibilityLink> links;
    @Mock
    private Iterator<AccessibilityLink> linkIterator;
    @Mock
    private NlsEvent publishEvent;
    @Captor
    private ArgumentCaptor<RoutingNetworkSettings<AccessibilityLink>> routingNetworkArgumentCaptor;

    @Mock
    private GraphHopperNetworkService indexedGraphHopperNetworkService;
    @Mock
    private AccessibilityLinkService accessibilityLinkService;
    @Mock
    private GraphHopperProperties graphHopperProperties;
    @Mock
    private MessageService messageService;
    @Mock
    private AccessibilityRoutingNetworkEventMapper accessibilityRoutingNetworkEventMapper;
    @InjectMocks
    private AccessibilityNetworkService accessibilityNetworkService;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        Files.deleteIfExists(Path.of(GRAPHHOPPER_DIR, NETWORK_NAME));
        Files.deleteIfExists(Path.of(GRAPHHOPPER_DIR));
    }

    @SneakyThrows
    @Test
    void storeLatestNetworkOnDisk_ok() {
        when(graphHopperProperties.getDir()).thenReturn(Path.of(GRAPHHOPPER_DIR));
        when(accessibilityLinkService.getLinks()).thenReturn(
                new AccessibilityLinkData(links, NWB_VERSION_ID, TRAFFIC_SIGN_TIMESTAMP));
        when(links.iterator()).thenReturn(linkIterator);

        when(accessibilityRoutingNetworkEventMapper.map(NWB_VERSION_ID, TRAFFIC_SIGN_TIMESTAMP))
                .thenReturn(publishEvent);
        when(publishEvent.getType()).thenReturn(ACCESSIBILITY_ROUTING_NETWORK_UPDATED);

        accessibilityNetworkService.storeLatestNetworkOnDisk();

        verify(indexedGraphHopperNetworkService).storeOnDisk(routingNetworkArgumentCaptor.capture());
        RoutingNetworkSettings<AccessibilityLink> routingNetwork = routingNetworkArgumentCaptor.getValue();
        assertEquals(NETWORK_NAME, routingNetwork.getNetworkNameAndVersion());
        assertEquals(AccessibilityLink.class, routingNetwork.getLinkType());
        assertEquals(List.of(PROFILE), routingNetwork.getProfiles());
        assertEquals(GRAPHHOPPER_DIR, routingNetwork.getGraphhopperRootPath().toString());
        assertEquals(linkIterator, routingNetwork.getLinkSupplier().get());
        assertEquals(TRAFFIC_SIGN_TIMESTAMP, routingNetwork.getDataDate());
        assertTrue(Files.exists(Path.of(GRAPHHOPPER_DIR, NETWORK_NAME)));

        verify(messageService).publish(publishEvent);
    }

}
