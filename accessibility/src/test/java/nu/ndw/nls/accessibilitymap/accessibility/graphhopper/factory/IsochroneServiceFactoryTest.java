package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.PROFILE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.graphhopper.util.PMap;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IsochroneServiceFactoryTest {

    @Mock
    NetworkGraphHopper network;

    @InjectMocks
    private IsochroneServiceFactory isochroneServiceFactory;

    @Test
    void createService() {
        isochroneServiceFactory.createService(network);

        verify(network).createWeighting(eq(PROFILE), any(PMap.class));
        verify(network).getEncodingManager();
    }
}