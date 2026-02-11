package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.CAR_PROFILE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.util.PMap;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IsochroneServiceFactoryTest {

    @Mock
    private AccessibilityNetwork accessibilityNetwork;

    @Mock
    private NetworkData networkData;

    @Mock
    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private NetworkGraphHopper network;

    @InjectMocks
    private IsochroneServiceFactory isochroneServiceFactory;

    @Test
    void createService() {

        when(accessibilityNetwork.getNetworkData()).thenReturn(networkData);
        when(networkData.getGraphHopperNetwork()).thenReturn(graphHopperNetwork);
        when(graphHopperNetwork.network()).thenReturn(network);

        isochroneServiceFactory.createService(accessibilityNetwork);

        verify(network).createWeighting(eq(CAR_PROFILE), any(PMap.class));
        verify(network).getEncodingManager();
    }
}
