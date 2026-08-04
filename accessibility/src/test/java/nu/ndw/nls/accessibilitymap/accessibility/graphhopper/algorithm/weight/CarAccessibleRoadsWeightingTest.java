package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.weight;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NwbNetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CarAccessibleRoadsWeightingTest {

    private static final double MIN_WEIGHT_PER_DISTANCE = 1.0;

    private static final double EDGE_WEIGHT = 12.5;

    private static final long EDGE_MILLIS = 123L;

    private static final double TURN_WEIGHT = 5.5;

    private static final long TURN_MILLIS = 456L;

    private static final int ROAD_SECTION_ID = 10;

    @Mock
    private Weighting sourceWeighting;

    @Mock
    private NwbNetworkData nwbNetworkData;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private EdgeIteratorState edgeState;

    @Mock
    private IntEncodedValue intEncodedValue;

    @Mock
    private AccessibilityNwbRoadSection roadSection;

    @Mock
    private EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    private CarAccessibleRoadsWeighting weighting;

    @BeforeEach
    void setUp() {
        weighting = new CarAccessibleRoadsWeighting(
                sourceWeighting,
                nwbNetworkData,
                encodingManager,
                edgeIteratorStateReverseExtractor);
    }

    @Test
    void calcMinWeightPerDistance() {
        when(sourceWeighting.calcMinWeightPerDistance()).thenReturn(MIN_WEIGHT_PER_DISTANCE);

        assertThat(weighting.calcMinWeightPerDistance())
                .isEqualTo(MIN_WEIGHT_PER_DISTANCE);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            false,false
            false,true
            true,false,
            true,true
            """)
    void calcEdgeWeight_accessible_returnsSourceWeight(boolean reverseSearch, boolean edgeIseReverse) {
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
        when(edgeState.get(intEncodedValue)).thenReturn(ROAD_SECTION_ID);
        when(roadSection.carriagewayTypeCode()).thenReturn(CarriagewayTypeCode.HR);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeState)).thenReturn(edgeIseReverse);

        if (reverseSearch ^ edgeIseReverse) {
            when(roadSection.backwardAccessible()).thenReturn(true);
        } else {
            when(roadSection.forwardAccessible()).thenReturn(true);
        }

        when(nwbNetworkData.findAccessibilityNwbRoadSectionById(ROAD_SECTION_ID))
                .thenReturn(Optional.of(roadSection));
        when(sourceWeighting.calcEdgeWeight(edgeState, reverseSearch))
                .thenReturn(EDGE_WEIGHT);

        assertThat(weighting.calcEdgeWeight(edgeState, reverseSearch))
                .isEqualTo(EDGE_WEIGHT);

        verify(sourceWeighting).calcEdgeWeight(edgeState, reverseSearch);
    }

    @Test
    void calcEdgeWeight_notAccessible_returnsInfinity() {
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
        when(edgeState.get(intEncodedValue)).thenReturn(ROAD_SECTION_ID);

        when(nwbNetworkData.findAccessibilityNwbRoadSectionById(ROAD_SECTION_ID))
                .thenReturn(Optional.of(roadSection));

        // Stub the properties used by IsCarAccessibleUtil so it returns false.

        assertThat(weighting.calcEdgeWeight(edgeState, false))
                .isEqualTo(Double.POSITIVE_INFINITY);

        verify(sourceWeighting, never()).calcEdgeWeight(any(), anyBoolean());
    }

    @Test
    void calcEdgeMillis() {
        when(sourceWeighting.calcEdgeMillis(edgeState, false))
                .thenReturn(EDGE_MILLIS);

        assertThat(weighting.calcEdgeMillis(edgeState, false))
                .isEqualTo(EDGE_MILLIS);
    }

    @Test
    void calcTurnWeight() {
        when(sourceWeighting.calcTurnWeight(1, 2, 3))
                .thenReturn(TURN_WEIGHT);

        assertThat(weighting.calcTurnWeight(1, 2, 3))
                .isEqualTo(TURN_WEIGHT);
    }

    @Test
    void calcTurnMillis() {
        when(sourceWeighting.calcTurnMillis(1, 2, 3))
                .thenReturn(TURN_MILLIS);

        assertThat(weighting.calcTurnMillis(1, 2, 3))
                .isEqualTo(TURN_MILLIS);
    }

    @Test
    void hasTurnCosts() {
        when(sourceWeighting.hasTurnCosts()).thenReturn(true);

        assertThat(weighting.hasTurnCosts()).isTrue();
    }

    @Test
    void getName() {
        when(sourceWeighting.getName()).thenReturn("fastest");

        assertThat(weighting.getName()).isEqualTo("fastest");
    }
}
