package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIterator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BooleanEdgeSetterTest {

    @Mock
    private EncodingManager encodingManager;
    @Mock
    private EdgeIterator edgeIterator;
    @Mock
    private BooleanEncodedValue booleanEncodedValue;

    @InjectMocks
    private BooleanEdgeSetter setter;

    @Test
    void getGetDataTypeClass_ok() {
        assertThat(setter.getGetDataTypeClass()).isEqualTo(Boolean.class);
    }

    @Test
    void getDefaultValue_ok() {
        assertThat(setter.getDefaultValue(booleanEncodedValue)).isFalse();
    }

    @Test
    void getEncoderType_ok() {
        assertThat(setter.getEncoderType()).isEqualTo(BooleanEncodedValue.class);
    }

    @Test
    void set_ok() {

        setter.set(edgeIterator, booleanEncodedValue, true);
        verify(edgeIterator).set(booleanEncodedValue, true);
    }

    @Test
    void setReverse_ok() {

        setter.setReverse(edgeIterator, booleanEncodedValue, true);
        verify(edgeIterator).setReverse(booleanEncodedValue, true);
    }
}
