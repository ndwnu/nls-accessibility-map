package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting;

import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.RB;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
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
class RoadDataWeightingDecoratorTest {

    private static final int LINK_ID = 123;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private Weighting sourceWeighting;

    @Mock
    private NwbData nwbData;

    @Mock
    private IntEncodedValue intEncodedValue;

    @Mock
    private AccessibilityNwbRoadSection accessibilityNwbRoadSection;

    private RoadDataWeightingDecorator roadDataWeightingDecorator;

    @BeforeEach
    void setUp() {
        roadDataWeightingDecorator = new RoadDataWeightingDecorator(sourceWeighting, nwbData, encodingManager);
    }

    @Test
    void calcMinWeightPerDistance() {

        roadDataWeightingDecorator.calcMinWeightPerDistance();

        verify(sourceWeighting).calcMinWeightPerDistance();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,true
            false,false
            true,false
            false,true
            """)
    void calcEdgeWeight_withRoadData(boolean isAccessible, boolean reversed) {
        try (var edgeAccessHandler = Mockito.mockStatic(EdgeAccessHandler.class)) {
            when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
            when(edgeIteratorState.get(intEncodedValue)).thenReturn(LINK_ID);
            when(accessibilityNwbRoadSection.carriagewayTypeCode()).thenReturn(RB);
            when(accessibilityNwbRoadSection.forwardAccessible()).thenReturn(true);
            when(accessibilityNwbRoadSection.backwardAccessible()).thenReturn(true);
            edgeAccessHandler.when(() -> EdgeAccessHandler.isAccessible(RB, true, true, reversed))
                    .thenReturn(isAccessible);
            when(nwbData.findAccessibilityNwbRoadSectionById(LINK_ID)).thenReturn(Optional.of(accessibilityNwbRoadSection));
            if (isAccessible) {
                when(sourceWeighting.calcEdgeWeight(edgeIteratorState, reversed)).thenReturn(1.0);
            }

            double weight = roadDataWeightingDecorator.calcEdgeWeight(edgeIteratorState, reversed);

            assertThat(weight).isEqualTo(isAccessible ? 1.0 : Double.POSITIVE_INFINITY);
        }
    }

    @Test
    void calcEdgeWeight_withoutRoadData_exception() {
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
        when(edgeIteratorState.get(intEncodedValue)).thenReturn(LINK_ID);
        when(nwbData.findAccessibilityNwbRoadSectionById(LINK_ID)).thenReturn(Optional.empty());

        assertThatIllegalStateException().isThrownBy(() -> roadDataWeightingDecorator.calcEdgeWeight(edgeIteratorState, true))
                .withMessage("Road section not found for link id: " + LINK_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void calcEdgeMillis(boolean reversed) {

        long weight = 345L;
        when(sourceWeighting.calcEdgeMillis(edgeIteratorState, reversed)).thenReturn(weight);

        assertThat(roadDataWeightingDecorator.calcEdgeMillis(edgeIteratorState, reversed)).isEqualTo(weight);
    }

    @Test
    void calcTurnWeight() {

        when(sourceWeighting.calcTurnWeight(1, 2, 3)).thenReturn(4D);

        assertThat(roadDataWeightingDecorator.calcTurnWeight(1, 2, 3)).isEqualTo(4D);
    }

    @Test
    void calcTurnMillis() {

        when(sourceWeighting.calcTurnMillis(1, 2, 3)).thenReturn(4L);

        assertThat(roadDataWeightingDecorator.calcTurnMillis(1, 2, 3)).isEqualTo(4L);
    }

    @Test
    void hasTurnCosts() {

        roadDataWeightingDecorator.hasTurnCosts();

        verify(sourceWeighting).hasTurnCosts();
    }

    @Test
    void getName() {

        roadDataWeightingDecorator.getName();

        verify(sourceWeighting).getName();
    }
}
