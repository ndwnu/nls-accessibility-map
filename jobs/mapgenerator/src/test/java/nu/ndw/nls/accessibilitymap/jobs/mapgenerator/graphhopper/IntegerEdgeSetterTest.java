package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIterator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IntegerEdgeSetterTest {

    private static final int INT_VALUE = 128;
    @Mock
    private EncodingManager encodingManager;
    @Mock
    private EdgeIterator edgeIterator;
    @Mock
    private IntEncodedValue intEncodedValue;

    @InjectMocks
    private IntegerEdgeSetter setter;

    @Test
    void getGetDataTypeClass_ok() {
        assertThat(setter.getGetDataTypeClass()).isEqualTo(Integer.class);
    }

    @Test
    void calculateDefaultValue_ok() {
        assertThat(setter.calculateDefaultValue(7)).isEqualTo(INT_VALUE);
    }

    @Test
    void getEncoderType_ok() {
        assertThat(setter.getEncoderType()).isEqualTo(IntEncodedValue.class);
    }

    @Test
    void set_ok() {
        setter.set(edgeIterator, intEncodedValue, INT_VALUE);

        verify(edgeIterator).set(intEncodedValue, INT_VALUE);
    }

    @Test
    void setReverse_ok() {
        setter.setReverse(edgeIterator, intEncodedValue, INT_VALUE);

        verify(edgeIterator).setReverse(intEncodedValue, INT_VALUE);
    }
}
