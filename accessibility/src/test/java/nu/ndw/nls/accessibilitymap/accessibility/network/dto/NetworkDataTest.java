package nu.ndw.nls.accessibilitymap.accessibility.network.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import java.util.Map;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("DataFlowIssue")
@ExtendWith(MockitoExtension.class)
class NetworkDataTest extends ValidationTest {

    private NetworkData networkData;

    @Mock
    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private NwbData nwbData;

    @Mock
    private NwbDataUpdates nwbDataUpdates;

    @BeforeEach
    void setUp() {

        when(graphHopperNetwork.nwbVersion()).thenReturn(1);
        when(graphHopperNetwork.network()).thenReturn(networkGraphHopper);
        when(nwbData.getNwbVersionId()).thenReturn(1);

        networkData = new NetworkData(graphHopperNetwork, nwbData, nwbDataUpdates);
    }

    @Test
    void constructor() {

        assertThat(networkData.getNwbVersion()).isEqualTo(1);
        assertThat(networkData.getNetworkGraphHopper()).isEqualTo(networkGraphHopper);
        assertThat(networkData.getNwbData()).isEqualTo(nwbData);
        assertThat(networkData.getNwbDataUpdates()).isEqualTo(nwbDataUpdates);
    }

    @Test
    void constructor_overloaded() {
        networkData = new NetworkData(networkGraphHopper, nwbData, nwbDataUpdates);
        assertThat(networkData.getNwbVersion()).isEqualTo(1);
        assertThat(networkData.getNetworkGraphHopper()).isEqualTo(networkGraphHopper);
        assertThat(networkData.getNwbData()).isEqualTo(nwbData);
        assertThat(networkData.getNwbDataUpdates()).isEqualTo(nwbDataUpdates);
    }

    @Test
    void constructor_notTheSameNwbVersion() {

        when(graphHopperNetwork.nwbVersion()).thenReturn(2);
        when(nwbData.getNwbVersionId()).thenReturn(1);

        assertThatThrownBy(() -> new NetworkData(graphHopperNetwork, nwbData, nwbDataUpdates))
                .hasMessage("Graph Hopper network and road sections do not match NWB versions.")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_graphHopperNetwork_null() {

        assertThatThrownBy(() -> new NetworkData((GraphHopperNetwork) null, nwbData, nwbDataUpdates))
                .hasMessage("graphHopperNetwork is marked non-null but is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_nwbData_null() {

        assertThatThrownBy(() -> new NetworkData(graphHopperNetwork, null, nwbDataUpdates))
                .hasMessage("nwbData is marked non-null but is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_nwbDataUpdates_null() {

        assertThatThrownBy(() -> new NetworkData(graphHopperNetwork, nwbData, null))
                .hasMessage("nwbDataUpdates is marked non-null but is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toStringTest() {

        assertThat(networkData).hasToString("NetworkData(nwbVersion=1, networkGraphHopper=networkGraphHopper)");
    }

    @Test
    void findGeometryInNetwork() {
        long roadSectionId = 1L;
        int edgeKey = 1;
        BaseGraph baseGraph = Mockito.mock(BaseGraph.class);
        EdgeIteratorState edgeIteratorState = Mockito.mock(EdgeIteratorState.class);
        PointList pointList = Mockito.mock(PointList.class);
        LineString lineString = Mockito.mock(LineString.class);
        when(networkGraphHopper.getWayIdToEdgeKey()).thenReturn(Map.of(roadSectionId, edgeKey));
        when(networkGraphHopper.getBaseGraph()).thenReturn(baseGraph);
        when(baseGraph.getEdgeIteratorStateForKey(edgeKey)).thenReturn(edgeIteratorState);
        when(edgeIteratorState.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);
        when(pointList.toLineString(false)).thenReturn(lineString);
        assertThat(networkData.findGeometryInNetwork(roadSectionId)).contains(lineString);
    }

    @Test
    void findGeometryInNetwork_empty() {
        long roadSectionId = 1L;
        int edgeKey = 1;
        when(networkGraphHopper.getWayIdToEdgeKey()).thenReturn(Map.of(roadSectionId, edgeKey));
        assertThat(networkData.findGeometryInNetwork(2)).isEmpty();
    }

    @Test
    void findCarriageWayTypeCodeByRoadSectionId_inRoadUpdates() {
        long roadSectionId = 1L;
        AccessibilityNwbRoadSectionUpdate accessibilityNwbRoadSectionUpdate = Mockito.mock(AccessibilityNwbRoadSectionUpdate.class);
        when(nwbDataUpdates.findChangedNwbRoadSectionById(roadSectionId)).thenReturn(Optional.of(accessibilityNwbRoadSectionUpdate));
        when(accessibilityNwbRoadSectionUpdate.carriagewayTypeCode()).thenReturn(CarriagewayTypeCode.RB);

        assertThat(networkData.findCarriageWayTypeCodeByRoadSectionId(roadSectionId)).contains(CarriagewayTypeCode.RB);
    }

    @Test
    void findCarriageWayTypeCodeByRoadSectionId_inNwbData() {
        long roadSectionId = 1L;
        AccessibilityNwbRoadSection accessibilityNwbRoadSection = Mockito.mock(AccessibilityNwbRoadSection.class);
        when(nwbDataUpdates.findChangedNwbRoadSectionById(roadSectionId)).thenReturn(Optional.empty());
        when(nwbData.findAccessibilityNwbRoadSectionById(roadSectionId)).thenReturn(Optional.of(accessibilityNwbRoadSection));
        when(accessibilityNwbRoadSection.carriagewayTypeCode()).thenReturn(CarriagewayTypeCode.RB);

        assertThat(networkData.findCarriageWayTypeCodeByRoadSectionId(roadSectionId)).contains(CarriagewayTypeCode.RB);
    }

    @Test
    void findCarriageWayTypeCodeByRoadSectionId_notfound() {
        long roadSectionId = 1L;
        when(nwbDataUpdates.findChangedNwbRoadSectionById(roadSectionId)).thenReturn(Optional.empty());
        when(nwbData.findAccessibilityNwbRoadSectionById(roadSectionId)).thenReturn(Optional.empty());

        assertThat(networkData.findCarriageWayTypeCodeByRoadSectionId(roadSectionId)).isEmpty();
    }



    @Override
    protected Class<?> getClassToTest() {
        return networkData.getClass();
    }
}
