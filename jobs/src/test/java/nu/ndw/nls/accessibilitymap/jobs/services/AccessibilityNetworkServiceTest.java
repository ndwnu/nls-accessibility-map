package nu.ndw.nls.accessibilitymap.jobs.services;

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
import nu.ndw.nls.accessibilitymap.jobs.services.AccessibilityLinkService.AccessibilityLinkData;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import nu.ndw.nls.routingmapmatcher.domain.model.RoutingNetwork;
import nu.ndw.nls.routingmapmatcher.graphhopper.AccessibilityGraphHopperNetworkService;
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
    private static final Instant TRAFFIC_SIGN_TIMESTAMP = Instant.parse("2023-11-07T15:37:23Z");

    @Mock
    private AccessibilityGraphHopperNetworkService accessibilityGraphHopperNetworkService;
    @Mock
    private AccessibilityLinkService accessibilityLinkService;

    private AccessibilityNetworkService accessibilityNetworkService;

    @Mock
    private List<Link> links;
    @Mock
    private Iterator<Link> linkIterator;
    @Captor
    private ArgumentCaptor<RoutingNetwork> routingNetworkArgumentCaptor;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        Files.deleteIfExists(Path.of(GRAPHHOPPER_DIR, NETWORK_NAME));
        Files.deleteIfExists(Path.of(GRAPHHOPPER_DIR));
        accessibilityNetworkService = new AccessibilityNetworkService(accessibilityGraphHopperNetworkService,
                accessibilityLinkService, GRAPHHOPPER_DIR);
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
    }
}
