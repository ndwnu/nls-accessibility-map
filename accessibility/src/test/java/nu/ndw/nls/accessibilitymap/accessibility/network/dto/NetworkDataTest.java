package nu.ndw.nls.accessibilitymap.accessibility.network.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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

    @Override
    protected Class<?> getClassToTest() {
        return networkData.getClass();
    }
}
