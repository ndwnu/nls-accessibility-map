package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIterator;
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
    private EdgeIterator edgeIterator;
    @Mock
    private DecimalEncodedValue decimalEncodedValue;

    @InjectMocks
    private DoubleEdgeSetter setter;

    @Test
    void getGetDataTypeClass_ok() {
        assertThat(setter.getGetDataTypeClass()).isEqualTo(Double.class);
    }

    @Test
    void calculateDefaultValue_ok() {
        assertThat(setter.calculateDefaultValue(7)).isEqualTo(DECIMAL_VALUE);
    }

    @Test
    void getEncoderType_ok() {
        assertThat(setter.getEncoderType()).isEqualTo(DecimalEncodedValue.class);
    }

    @Test
    void set_ok() {
        setter.set(edgeIterator, decimalEncodedValue, DECIMAL_VALUE);

        verify(edgeIterator).set(decimalEncodedValue, DECIMAL_VALUE);
    }

    @Test
    void setReverse_ok() {
        setter.setReverse(edgeIterator, decimalEncodedValue, DECIMAL_VALUE);

        verify(edgeIterator).setReverse(decimalEncodedValue, DECIMAL_VALUE);
    }
}
