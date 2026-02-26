package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.isochone;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.REVERSED_LINK_ID;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IsoLabelToRoadSectionIdMapperTest {

    private IsoLabelToRoadSectionIdMapper isoLabelToRoadSectionIdMapper;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private IntEncodedValue intEncodedValueWayId;

    @Mock
    private IntEncodedValue intEncodedValueReversedWayId;

    @BeforeEach
    void setUp() {

        isoLabelToRoadSectionIdMapper = new IsoLabelToRoadSectionIdMapper();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            false, false, false, 1,
            true, false, false, 1,
            false, true, false, 1,
            false, false, true, 1,
            true, true, false, 1,
            false, true, true, 2,
            true, false, true, 2,
            true, true, true, 1,
            """)
    void map(boolean isReversed, boolean isochroneCalculatedInReverse, boolean hasReversedLinkId, int expectedRoadSectionId) {

        if ((isReversed != isochroneCalculatedInReverse) && hasReversedLinkId) {
            when(encodingManager.getIntEncodedValue(REVERSED_LINK_ID)).thenReturn(intEncodedValueReversedWayId);
            when(edgeIteratorState.get(intEncodedValueReversedWayId)).thenReturn(2);
        } else {
            if (isReversed != isochroneCalculatedInReverse) {
                when(encodingManager.getIntEncodedValue(REVERSED_LINK_ID)).thenReturn(intEncodedValueReversedWayId);
                when(edgeIteratorState.get(intEncodedValueReversedWayId)).thenReturn(0);
            }
            when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValueWayId);
            when(edgeIteratorState.get(intEncodedValueWayId)).thenReturn(1);
        }

        int roadSectionId = isoLabelToRoadSectionIdMapper.map(edgeIteratorState, encodingManager, isReversed, isochroneCalculatedInReverse);

        assertThat(roadSectionId).isEqualTo(expectedRoadSectionId);
    }
}
