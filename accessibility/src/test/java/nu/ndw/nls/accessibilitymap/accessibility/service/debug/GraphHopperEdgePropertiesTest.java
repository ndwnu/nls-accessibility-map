package nu.ndw.nls.accessibilitymap.accessibility.service.debug;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphHopperEdgePropertiesTest {

    @Test
    void constructor() {

        GraphHopperEdgeProperties graphHopperEdgeProperties = new GraphHopperEdgeProperties(1, 2, 3, 4, 5);

        assertThat(graphHopperEdgeProperties.getType()).isEqualTo("edge");
        assertThat(graphHopperEdgeProperties.getEdge()).isEqualTo(1);
        assertThat(graphHopperEdgeProperties.getEdgeKey()).isEqualTo(2);
        assertThat(graphHopperEdgeProperties.getFromNode()).isEqualTo(3);
        assertThat(graphHopperEdgeProperties.getToNode()).isEqualTo(4);
        assertThat(graphHopperEdgeProperties.getDistance()).isEqualTo(5);
    }
}
