package nu.ndw.nls.accessibilitymap.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperConfiguration;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityMapConfigurationTest {

    @Mock
    private GraphHopperConfiguration graphHopperConfiguration;

    @Mock
    private GraphHopperNetworkService graphHopperNetworkService;

    @InjectMocks
    private AccessibilityMapConfiguration accessibilityMapConfiguration;

    @Mock
    private RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings;

    @Mock
    private NetworkGraphHopper networkGraphHopper;


    @Test
    @SneakyThrows
    void networkGraphHopper_ok() {
        when(graphHopperConfiguration.configureLoadingRoutingNetworkSettings()).thenReturn(routingNetworkSettings);
        when(graphHopperNetworkService.loadFromDisk(routingNetworkSettings)).thenReturn(networkGraphHopper);
        assertEquals(networkGraphHopper, accessibilityMapConfiguration.networkGraphHopper());
    }
}