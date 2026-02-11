package nu.ndw.nls.accessibilitymap.accessibility.network.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NetworkDataTest extends ValidationTest {

    private NetworkData networkData;

    @Mock
    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private NwbData nwbData;

    @BeforeEach
    void setUp() {

        when(graphHopperNetwork.nwbVersion()).thenReturn(1);
        when(nwbData.getNwbVersionId()).thenReturn(1);

        networkData = new NetworkData(graphHopperNetwork, nwbData);
    }

    @Test
    void constructor() {

        assertThat(networkData.getNwbVersion()).isEqualTo(1);
        assertThat(networkData.getGraphHopperNetwork()).isEqualTo(graphHopperNetwork);
        assertThat(networkData.getNwbData()).isEqualTo(nwbData);
    }

    @Test
    void constructor_notTheSameNwbVersion() {

        when(graphHopperNetwork.nwbVersion()).thenReturn(2);
        when(nwbData.getNwbVersionId()).thenReturn(1);

        assertThatThrownBy(() -> new NetworkData(graphHopperNetwork, nwbData))
                .hasMessage("Graph Hopper network and road sections do not match NWB versions.")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_graphHopperNetwork_null() {

        assertThatThrownBy(() -> new NetworkData(null, nwbData))
                .hasMessage("graphHopperNetwork is marked non-null but is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_nwbData_null() {

        assertThatThrownBy(() -> new NetworkData(graphHopperNetwork, null))
                .hasMessage("nwbData is marked non-null but is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toStringTest() {

        assertThat(networkData).hasToString("NetworkData(nwbVersion=1, graphHopperNetwork=graphHopperNetwork)");
    }

    @Override
    protected Class<?> getClassToTest() {
        return networkData.getClass();
    }
}
