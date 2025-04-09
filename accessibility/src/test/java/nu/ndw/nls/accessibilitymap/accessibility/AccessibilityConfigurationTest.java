package nu.ndw.nls.accessibilitymap.accessibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperNetworkSettingsBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphhopperConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityConfigurationTest {

    @Mock
    private GraphHopperNetworkSettingsBuilder graphHopperConfiguration;

    @Mock
    private GraphHopperNetworkService graphHopperNetworkService;

    @Mock
    private NetworkMetaDataService networkMetaDataService;

    @InjectMocks
    private GraphhopperConfiguration accessibilityConfiguration;

    @Mock
    private RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private GraphhopperMetaData graphhopperMetaData;

    @Test
    @SneakyThrows
    void networkGraphHopper() {

        when(graphHopperConfiguration.buildDefaultNetworkSettings()).thenReturn(routingNetworkSettings);
        when(graphHopperNetworkService.loadFromDisk(routingNetworkSettings)).thenReturn(networkGraphHopper);

        assertEquals(networkGraphHopper, accessibilityConfiguration.networkGraphHopper());
    }

    @Test
    @SneakyThrows
    void getMetaData() {

        when(networkMetaDataService.loadMetaData()).thenReturn(graphhopperMetaData);

        assertEquals(graphhopperMetaData, accessibilityConfiguration.getMetaData());
    }

    @Test
    void edgeIteratorStateReverseExtractor() {

        assertNotNull(accessibilityConfiguration.edgeIteratorStateReverseExtractor());

    }
}