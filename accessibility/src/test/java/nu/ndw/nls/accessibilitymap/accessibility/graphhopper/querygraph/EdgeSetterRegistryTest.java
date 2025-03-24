package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EdgeSetterRegistryTest {

    @Mock
    private EdgeSetter<Boolean, BooleanEncodedValue> edgeSetter;

    private EdgeSetterRegistry registry;

    @BeforeEach
    void setUp() {
        when(edgeSetter.getGetDataTypeClass()).thenReturn(Boolean.class);

        registry = new EdgeSetterRegistry(List.of(edgeSetter));

    }

    @Test
    void getEdgeSetter() {
        assertThat(registry.getEdgeSetter(Boolean.class))
                .isNotEmpty()
                .contains(edgeSetter);
    }
}
