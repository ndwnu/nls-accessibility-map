package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.isochone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import nu.ndw.nls.routingmapmatcher.util.PointListUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IsoLabelToGeometryMapperTest {

    private IsoLabelToGeometryMapper isoLabelToGeometryMapper;

    @Mock
    private PointListUtil pointListUtil;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private PointList pointList;

    @Mock
    private LineString lineString;

    @BeforeEach
    void setUp() {

        isoLabelToGeometryMapper = new IsoLabelToGeometryMapper(pointListUtil);
    }

    @Test
    void map() {

        when(edgeIteratorState.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);
        when(pointListUtil.toLineString(pointList)).thenReturn(lineString);

        assertThat(isoLabelToGeometryMapper.map(edgeIteratorState)).isEqualTo(lineString);
    }
}
