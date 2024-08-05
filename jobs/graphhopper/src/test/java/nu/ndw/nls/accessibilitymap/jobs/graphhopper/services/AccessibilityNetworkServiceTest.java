package nu.ndw.nls.accessibilitymap.jobs.graphhopper.services;

import static nu.ndw.nls.events.NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.mapper.AccessibilityRoutingNetworkEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.services.AccessibilityLinkService.AccessibilityLinkData;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.shared.network.services.NetworkMetaDataService;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperConfiguration;
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
    @Mock
    private GraphHopperNetworkService indexedGraphHopperNetworkService;
    @Mock
    private AccessibilityLinkService accessibilityLinkService;
    @Mock
    private GraphHopperConfiguration graphHopperConfiguration;
    @Mock
    private MessageService messageService;
    @Mock
    private AccessibilityRoutingNetworkEventMapper accessibilityRoutingNetworkEventMapper;
    @Mock
    private NetworkMetaDataService networkMetaDataService;

    @InjectMocks
    private AccessibilityNetworkService accessibilityNetworkService;

    @Mock
    private RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings;

    @Captor
    ArgumentCaptor<Supplier<Iterator<AccessibilityLink>>> supplierArgumentCaptor;

    private Path tmpLatestPathFolder;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        tmpLatestPathFolder = Files.createTempDirectory("test-accessibility-network-service");
        when(graphHopperConfiguration.getLatestPath()).thenReturn(tmpLatestPathFolder);
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

        when(graphHopperConfiguration.configurePersistingRoutingNetworkSettings(any(), eq(TRAFFIC_SIGN_TIMESTAMP)))
                .thenReturn(routingNetworkSettings);

        accessibilityNetworkService.storeLatestNetworkOnDisk();

        verify(networkMetaDataService).saveMetaData(new AccessibilityGraphhopperMetaData(NWB_VERSION_ID));

        verify(graphHopperConfiguration).configurePersistingRoutingNetworkSettings(supplierArgumentCaptor.capture(),
                eq(TRAFFIC_SIGN_TIMESTAMP));

        assertEquals(linkIterator, supplierArgumentCaptor.getValue().get());

        verify(indexedGraphHopperNetworkService).storeOnDisk(routingNetworkSettings);
        assertTrue(Files.exists(tmpLatestPathFolder));

        verify(messageService).publish(publishEvent);
    }

}
