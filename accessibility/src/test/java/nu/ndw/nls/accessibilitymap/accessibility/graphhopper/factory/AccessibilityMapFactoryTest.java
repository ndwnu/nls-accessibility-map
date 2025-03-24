package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityMapFactoryTest {

    @Mock
    NetworkGraphHopper network;
    @Mock
    private IsochroneServiceFactory isochroneServiceFactory;

    @InjectMocks
    private AccessibilityMapFactory accessibilityMapFactory;

    @Test
    void createMapMatcher() {
        accessibilityMapFactory.createMapMatcher(network);
        verify(isochroneServiceFactory).createService(network);
    }

    @Test
    void createMapMatcher_exception_networkNull() {
        assertThrows(Exception.class, () -> accessibilityMapFactory.createMapMatcher(null));
    }

}