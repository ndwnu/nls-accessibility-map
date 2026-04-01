package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util.LinkIdResolver.resolveLinkId;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.REVERSED_LINK_ID;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

class LinkIdResolverTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
            1,2,false,1
            1,2,true,2
            1,-1,true,1
            1,-1,false,1
            """)
    void resolveLinkId_ok(int linkId, int reversedLinkId, boolean isReversed, int expectedLinkId) {
        EdgeIteratorState edge = Mockito.mock(EdgeIteratorState.class);
        EncodingManager encodingManager = Mockito.mock(EncodingManager.class);
        IntEncodedValue linkIdEncodedValue = Mockito.mock(IntEncodedValue.class);
        IntEncodedValue reversedLinkIdEncodedValue = Mockito.mock(IntEncodedValue.class);

        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(linkIdEncodedValue);
        when(encodingManager.getIntEncodedValue(REVERSED_LINK_ID)).thenReturn(reversedLinkIdEncodedValue);
        when(edge.get(linkIdEncodedValue)).thenReturn(linkId);
        when(edge.get(reversedLinkIdEncodedValue)).thenReturn(reversedLinkId);

        assertThat(resolveLinkId(edge, encodingManager, isReversed)).isEqualTo(expectedLinkId);
    }
}
