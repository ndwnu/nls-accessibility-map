package nu.ndw.nls.accessibilitymap.accessibility.network.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.util.EncodingManager;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetworkWithVersion;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
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
    private GraphHopperNetworkWithVersion graphHopperNetworkWithVersion;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private NwbData nwbData;

    @BeforeEach
    void setUp() {

        when(graphHopperNetworkWithVersion.nwbVersion()).thenReturn(1);
        when(graphHopperNetworkWithVersion.network()).thenReturn(networkGraphHopper);
        when(nwbData.getNwbVersionId()).thenReturn(1);

        networkData = new NetworkData(graphHopperNetworkWithVersion, nwbData);
    }

    @Test
    void constructor() {

        assertThat(networkData.getNwbVersion()).isEqualTo(1);
        assertThat(networkData.getNetworkGraphHopper()).isEqualTo(networkGraphHopper);
        assertThat(networkData.getNwbData()).isEqualTo(nwbData);
    }

    @Test
    void constructor_notTheSameNwbVersion() {

        when(graphHopperNetworkWithVersion.nwbVersion()).thenReturn(2);
        when(nwbData.getNwbVersionId()).thenReturn(1);

        assertThatThrownBy(() -> new NetworkData(graphHopperNetworkWithVersion, nwbData))
                .hasMessage("Graph Hopper network and road sections do not match NWB versions.")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_graphHopperNetwork_null() {

        assertThatThrownBy(() -> new NetworkData(null, nwbData))
                .hasMessage("graphHopperNetworkWithVersion is marked non-null but is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_nwbData_null() {

        assertThatThrownBy(() -> new NetworkData(graphHopperNetworkWithVersion, null))
                .hasMessage("nwbData is marked non-null but is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getEncodingManager() {

        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);

        assertThat(networkData.getEncodingManager()).isNotNull().isEqualTo(encodingManager);
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
