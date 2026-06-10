package nu.ndw.nls.accessibilitymap.accessibility.service.debug;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphHopperNodePropertiesTest {

    @Test
    void constructor() {

        GraphHopperNodeProperties graphHopperNodeProperties = new GraphHopperNodeProperties(1);

        assertThat(graphHopperNodeProperties.getType()).isEqualTo("node");
        assertThat(graphHopperNodeProperties.getId()).isEqualTo(1);
    }
}
