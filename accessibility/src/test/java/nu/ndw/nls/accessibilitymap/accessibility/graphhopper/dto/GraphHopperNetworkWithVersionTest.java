package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphHopperNetworkWithVersionTest extends ValidationTest {

    private GraphHopperNetworkWithVersion graphHopperNetworkWithVersion;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @BeforeEach
    void setUp() {

        graphHopperNetworkWithVersion = new GraphHopperNetworkWithVersion(
                networkGraphHopper,
                1
        );
    }

    @Test
    void validate() {

        validate(graphHopperNetworkWithVersion, List.of(), List.of());
    }

    @Test
    void validate_network_null() {

        graphHopperNetworkWithVersion = graphHopperNetworkWithVersion.withNetwork(null);
        validate(graphHopperNetworkWithVersion, List.of("network"), List.of("must not be null"));
    }

    @Test
    void validate_nwbVersion_null() {

        graphHopperNetworkWithVersion = graphHopperNetworkWithVersion.withNwbVersion(null);
        validate(graphHopperNetworkWithVersion, List.of("nwbVersion"), List.of("must not be null"));
    }

    @Test
    void toStringTest() {
        assertThat(graphHopperNetworkWithVersion)
                .hasToString("GraphHopperNetwork(nwbVersion=1)");
    }

    @Override
    protected Class<?> getClassToTest() {
        return graphHopperNetworkWithVersion.getClass();
    }
}
