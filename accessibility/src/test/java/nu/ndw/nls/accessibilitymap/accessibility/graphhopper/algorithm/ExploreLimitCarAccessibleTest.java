package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NwbNetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExploreLimitCarAccessibleTest {

    private ExploreLimitCarAccessible exploreLimitCarAccessible;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private NwbNetworkData nwbNetworkData;

    @Mock
    private EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private Restriction restriction;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private IntEncodedValue intEncodedValue;

    @BeforeEach
    void setUp() {

        exploreLimitCarAccessible = new ExploreLimitCarAccessible(queryGraph, nwbNetworkData, edgeIteratorStateReverseExtractor);
    }

    @Test
    void isInLimit_inLimit() {
        RestrictionsIsochroneLabel label = createRestrictionsIsochroneLabel(new Restrictions());

        mockWithinLimit(label);

        assertThat(exploreLimitCarAccessible.isInLimit(label, encodingManager)).isTrue();
    }

    @Test
    void isInLimit_notInLimit() {
        RestrictionsIsochroneLabel label = createRestrictionsIsochroneLabel(new Restrictions());

        mockNotWithinLimit(label);

        assertThat(exploreLimitCarAccessible.isInLimit(label, encodingManager)).isFalse();
    }

    private static RestrictionsIsochroneLabel createRestrictionsIsochroneLabel(Restrictions restrictions) {
        return new RestrictionsIsochroneLabel(0, 5, 5, null, 0L, 0.0, 0.0, restrictions);
    }

    @Test
    void debug() {

        RestrictionsIsochroneLabel label = createRestrictionsIsochroneLabel(new Restrictions());

        mockWithinLimit(label);

        assertThat(exploreLimitCarAccessible.debug(label, encodingManager)).isEqualTo(
                "ExploreLimitCarAccessible{limit=1.0, roadSectionId=3, carriagewayTypeCode=RB, reached=false}");
    }

    @Test
    void debug_limitReached() {

        RestrictionsIsochroneLabel label = createRestrictionsIsochroneLabel(new Restrictions());

        mockNotWithinLimit(label);

        assertThat(exploreLimitCarAccessible.debug(label, encodingManager)).isEqualTo(
                "ExploreLimitCarAccessible{limit=1.0, roadSectionId=3, carriagewayTypeCode=FP, reached=true}");
    }

    private void mockWithinLimit(RestrictionsIsochroneLabel label) {
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
        when(queryGraph.getEdgeIteratorState(label.getEdge(), label.getNode())).thenReturn(edgeIteratorState);
        when(queryGraph.getEdgeIteratorStateForKey(label.getEdgeKey())).thenReturn(edgeIteratorState);
        when(edgeIteratorState.get(intEncodedValue)).thenReturn(3);
        when(nwbNetworkData.findAccessibilityNwbRoadSectionById(3)).thenReturn(Optional.of(AccessibilityNwbRoadSection.builder()
                .forwardAccessible(true)
                .backwardAccessible(true)
                .carriagewayTypeCode(CarriagewayTypeCode.RB)
                .build()));
    }

    private void mockNotWithinLimit(RestrictionsIsochroneLabel label) {
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
        when(queryGraph.getEdgeIteratorState(label.getEdge(), label.getNode())).thenReturn(edgeIteratorState);
        when(queryGraph.getEdgeIteratorStateForKey(label.getEdgeKey())).thenReturn(edgeIteratorState);
        when(edgeIteratorState.get(intEncodedValue)).thenReturn(3);
        when(nwbNetworkData.findAccessibilityNwbRoadSectionById(3)).thenReturn(Optional.of(AccessibilityNwbRoadSection.builder()
                .carriagewayTypeCode(CarriagewayTypeCode.FP)
                .build()));
    }
}
