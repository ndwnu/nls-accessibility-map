package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DoubleEdgeSetterTest {

    private static final double DECIMAL_VALUE = 128D;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private DecimalEncodedValue decimalEncodedValue;

    @InjectMocks
    private DoubleEdgeSetter setter;

    @Test
    void getGetDataTypeClass_ok() {
        assertThat(setter.getGetDataTypeClass()).isEqualTo(Double.class);
    }

    @Test
    void getDefaultValue_ok() {
        when(decimalEncodedValue.getMaxStorableDecimal()).thenReturn(DECIMAL_VALUE);
        assertThat(setter.getDefaultValue(decimalEncodedValue)).isEqualTo(DECIMAL_VALUE);
    }

    @Test
    void getEncoderType_ok() {
        assertThat(setter.getEncoderType()).isEqualTo(DecimalEncodedValue.class);
    }

    @Test
    void set_ok() {

        setter.set(edgeIteratorState, decimalEncodedValue, DECIMAL_VALUE);

        verify(edgeIteratorState).set(decimalEncodedValue, DECIMAL_VALUE);
    }

    @Test
    void setReverse_ok() {

        setter.setReverse(edgeIteratorState, decimalEncodedValue, DECIMAL_VALUE);

        verify(edgeIteratorState).setReverse(decimalEncodedValue, DECIMAL_VALUE);
    }
}
