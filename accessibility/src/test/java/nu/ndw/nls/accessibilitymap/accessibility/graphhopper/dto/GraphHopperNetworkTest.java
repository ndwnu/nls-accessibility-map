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
class GraphHopperNetworkTest extends ValidationTest {

    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @BeforeEach
    void setUp() {

        graphHopperNetwork = new GraphHopperNetwork(
                networkGraphHopper,
                1
        );
    }

    @Test
    void validate() {

        validate(graphHopperNetwork, List.of(), List.of());
    }

    @Test
    void validate_network_null() {

        graphHopperNetwork = graphHopperNetwork.withNetwork(null);
        validate(graphHopperNetwork, List.of("network"), List.of("must not be null"));
    }

    @Test
    void validate_nwbVersion_null() {

        graphHopperNetwork = graphHopperNetwork.withNwbVersion(null);
        validate(graphHopperNetwork, List.of("nwbVersion"), List.of("must not be null"));
    }

    @Test
    void toStringTest() {
        assertThat(graphHopperNetwork)
                .hasToString("GraphHopperNetwork(nwbVersion=1)");
    }

    @Override
    protected Class<?> getClassToTest() {
        return graphHopperNetwork.getClass();
    }
}
