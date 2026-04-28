package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting;

import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.RB;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NwbDataUpdatesWeightingDecoratorTest {

    private static final int LINK_ID = 123;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private Weighting sourceWeighting;

    @Mock
    private NwbDataUpdates nwbDataUpdates;

    @Mock
    private IntEncodedValue intEncodedValue;

    @Mock
    private AccessibilityNwbRoadSectionUpdate accessibilityNwbRoadSectionUpdate;

    private RoadChangesWeighting roadChangesWeighting;

    @BeforeEach
    void setUp() {

        roadChangesWeighting = new RoadChangesWeighting(sourceWeighting, nwbDataUpdates, encodingManager);
    }

    @Test
    void calcMinWeightPerDistance() {

        roadChangesWeighting.calcMinWeightPerDistance();

        verify(sourceWeighting).calcMinWeightPerDistance();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,true
            false,false
            true,false
            false,true
            """)
    void calcEdgeWeight_with_roadChanges(boolean reversed, boolean hasAccess) {

        try (var edgeAccessHandler = Mockito.mockStatic(EdgeAccessHandler.class)) {
            when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
            when(edgeIteratorState.get(intEncodedValue)).thenReturn(LINK_ID);
            when(accessibilityNwbRoadSectionUpdate.carriagewayTypeCode()).thenReturn(RB);
            when(accessibilityNwbRoadSectionUpdate.forwardAccessible()).thenReturn(true);
            when(accessibilityNwbRoadSectionUpdate.backwardAccessible()).thenReturn(true);
            edgeAccessHandler.when(() -> EdgeAccessHandler.isAccessible(RB, true, true, reversed))
                    .thenReturn(hasAccess);
            when(nwbDataUpdates.findChangedNwbRoadSectionById(LINK_ID)).thenReturn(Optional.of(accessibilityNwbRoadSectionUpdate));
            if (hasAccess) {
                when(sourceWeighting.calcEdgeWeight(edgeIteratorState, reversed)).thenReturn(1.0);
            }

            double weight = roadChangesWeighting.calcEdgeWeight(edgeIteratorState, reversed);

            assertThat(weight).isEqualTo(hasAccess ? 1.0 : Double.POSITIVE_INFINITY);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void calcEdgeWeight_with_no_roadChanges(boolean reversed) {

        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
        when(edgeIteratorState.get(intEncodedValue)).thenReturn(LINK_ID);
        when(nwbDataUpdates.findChangedNwbRoadSectionById(LINK_ID)).thenReturn(Optional.empty());
        when(sourceWeighting.calcEdgeWeight(edgeIteratorState, reversed)).thenReturn(1.0);

        double weight = roadChangesWeighting.calcEdgeWeight(edgeIteratorState, reversed);

        assertThat(weight).isEqualTo(1.0);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void calcEdgeMillis(boolean reversed) {

        long weight = 345L;
        when(sourceWeighting.calcEdgeMillis(edgeIteratorState, reversed)).thenReturn(weight);

        assertThat(roadChangesWeighting.calcEdgeMillis(edgeIteratorState, reversed)).isEqualTo(weight);
    }

    @Test
    void calcTurnWeight() {

        when(sourceWeighting.calcTurnWeight(1, 2, 3)).thenReturn(4D);

        assertThat(roadChangesWeighting.calcTurnWeight(1, 2, 3)).isEqualTo(4D);
    }

    @Test
    void calcTurnMillis() {

        when(sourceWeighting.calcTurnMillis(1, 2, 3)).thenReturn(4L);

        assertThat(roadChangesWeighting.calcTurnMillis(1, 2, 3)).isEqualTo(4L);
    }

    @Test
    void hasTurnCosts() {

        roadChangesWeighting.hasTurnCosts();

        verify(sourceWeighting).hasTurnCosts();
    }

    @Test
    void getName() {

        roadChangesWeighting.getName();

        verify(sourceWeighting).getName();
    }
}
