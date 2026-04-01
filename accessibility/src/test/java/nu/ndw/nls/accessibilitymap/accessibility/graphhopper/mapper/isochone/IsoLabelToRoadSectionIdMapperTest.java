package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.isochone;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @BeforeEach
    void setUp() {

        isoLabelToRoadSectionIdMapper = new IsoLabelToRoadSectionIdMapper();
    }

    @Test
    void map() {

        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValueWayId);
        when(edgeIteratorState.get(intEncodedValueWayId)).thenReturn(1);

        int roadSectionId = isoLabelToRoadSectionIdMapper.map(edgeIteratorState, encodingManager);

        assertThat(roadSectionId).isEqualTo(1);
    }
}
