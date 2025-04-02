package nu.ndw.nls.accessibilitymap.jobs.graphhopper.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityLinkCarMapperTest {

    @Mock
    private AccessibilityLink link;
    @Mock
    private DirectionalDto<Boolean> accessibility;

    @InjectMocks
    private AccessibilityLinkCarMapper mapper;

    @Test
    void getAccessibility() {
        when(link.getAccessibility()).thenReturn(accessibility);
        assertEquals(accessibility, mapper.getAccessibility(link));
    }

    @Test
    void getSpeed() {
        DirectionalDto<Double> result = mapper.getSpeed(link);
        assertEquals(50, result.forward());
        assertEquals(50, result.reverse());
    }

}